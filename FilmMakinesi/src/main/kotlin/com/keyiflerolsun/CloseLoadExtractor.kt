package com.keyiflerolsun

import android.util.Base64
import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*


class CloseLoadExtractor : ExtractorApi() {
    override val mainUrl = "https://closeload.filmmakinesi.to"
    override val name = "CloseLoad"
    override val requiresReferer = true

    companion object {
        /** Sayfa bazen yalnızca ölü "playmix" adresi döndürüyor; o durumda CDN adresi kalıptan kurulur. */
        private const val MAKS_DENEME = 6

        /** Yeni CloseLoad oynatıcısının CDN'i (c78 çözümünden öğrenilir, bu yalnızca yedek). */
        private const val VARSAYILAN_CDN = "srv9.cdnimages3865.cyou"

        private const val UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

        /** çalışan bir CDN'den öğrenilen alan adı (uygulama açık kaldıkça hatırlanır) */
        @Volatile
        private var ogrenilenCdn: String? = null
    }

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        Log.d(name, "getUrl çağrıldı, url: $url")

        var videoUrl: String? = null
        var masterBody: String? = null
        // Altyazılar her sürümde var; link bulunamasa bile altyazı gösterilebilsin.
        var subtitleHtml: String? = null

        // Sunucu her istekte farklı bir şifreleme sürümü döndürüyor (c78 / eski g867).
        // Sürümlerden birinin çözdüğü CDN adresi ölü olabiliyor (playmix 404),
        // bu yüzden çalışan (HTTP 200 + #EXTM3U) bir manifest bulana kadar sayfayı yeniliyoruz.
        for (deneme in 1..MAKS_DENEME) {
            val sayfaUrl = if (deneme == 1) {
                url
            } else {
                val ayrac = if (url.contains("?")) "&" else "?"
                "$url${ayrac}_=${System.currentTimeMillis()}$deneme"
            }

            val rawHtml = try {
                app.get(sayfaUrl, referer = referer ?: "", headers = mapOf(
                    "Cache-Control" to "no-cache",
                    "Pragma" to "no-cache"
                )).text
            } catch (e: Exception) {
                Log.w(name, "Sayfa alınamadı (deneme $deneme): ${e.message}")
                continue
            }

            if (subtitleHtml == null || rawHtml.contains("tracks:")) subtitleHtml = rawHtml

            val adaylar = LinkedHashSet<String>()
            adaylar += adaylariCoz(rawHtml, sayfaUrl)

            // Sayfadaki görsel adı dosya adıyla aynı: /img/<slug>.jpg -> /hls/<slug>.mp4/master.txt
            val slug = Regex("""/img/([A-Za-z0-9_\-]+)\.(?:jpg|jpeg|png|webp)""")
                .find(rawHtml)?.groupValues?.get(1)
            if (slug != null) {
                val konaklar = (listOfNotNull(ogrenilenCdn) + VARSAYILAN_CDN).distinct()
                for (konak in konaklar) {
                    adaylar += "https://$konak/hls/$slug.mp4/master.txt"
                }
            }

            Log.d(name, "Deneme $deneme/$MAKS_DENEME - ${rawHtml.length} bayt, ${adaylar.size} aday")
            adaylar.forEach { Log.d(name, "  aday: $it") }

            for (aday in adaylar) {
                val masterResponse = try {
                    app.get(aday, referer = sayfaUrl, headers = mapOf(
                        "Accept" to "*/*",
                        "Origin" to "https://closeload.filmmakinesi.to"
                    ))
                } catch (e: Exception) {
                    Log.d(name, "Aday alınamadı: $aday (${e.message})")
                    continue
                }

                val govde = masterResponse.text
                if (masterResponse.code == 200 && govde.contains("#EXTM3U")) {
                    videoUrl = aday
                    masterBody = govde
                    subtitleHtml = rawHtml
                    Regex("""//([^/]+)/hls/""").find(aday)?.groupValues?.get(1)
                        ?.let { ogrenilenCdn = it }
                    Log.d(name, "Çalışan manifest bulundu: $aday")
                    break
                }
                Log.d(name, "Aday ölü (${masterResponse.code}): $aday")
            }

            if (videoUrl != null) break
        }

        if (videoUrl == null) {
            Log.e(name, "$MAKS_DENEME denemede çalışan manifest bulunamadı")
        } else {
            Log.d(name, "Master body (ilk 500):\n${masterBody?.take(500)}")
        }

        val rawHtml = subtitleHtml ?: ""
        val tracksMatch = Regex("""tracks:\s*\[(.*?)\]""", RegexOption.DOT_MATCHES_ALL).find(rawHtml)
        val tracksStr = tracksMatch?.groupValues?.get(1)

        tracksStr?.let { str ->
            val matches = Regex(""""file"\s*:\s*"([^"]+)".*?"label"\s*:\s*"([^"]+)"""", RegexOption.DOT_MATCHES_ALL)
                .findAll(str).toList()
            Log.d(name, "Bulunan altyazı sayısı: ${matches.size}")

            matches.forEachIndexed { index, match ->
                val subUrl = match.groupValues[1].replace("\\/", "/")
                val subLabel = match.groupValues[2]
                val lang = when {
                    subLabel.contains("Forced", ignoreCase = true) -> "Türkçe (zorunlu)"
                    subLabel.contains("Turkish", ignoreCase = true) -> "Türkçe"
                    subLabel.contains("English", ignoreCase = true) -> "İngilizce"
                    subLabel.contains("French", ignoreCase = true) -> "Fransızca"
                    subLabel.contains("Spanish", ignoreCase = true) -> "İspanyolca"
                    subLabel.contains("German", ignoreCase = true) -> "Almanca"
                    subLabel.contains("Portuguese", ignoreCase = true) -> "Portekizce"
                    else -> return@forEachIndexed
                }
                Log.d(name, "Altyazı #$index - lang: '$lang', label: '$subLabel'")
                // VTT sunucusu referer olmadan 403 dönüyor
                subtitleCallback.invoke(
                    newSubtitleFile(lang = lang, url = subUrl) {
                        headers = mapOf("Referer" to url, "User-Agent" to UA)
                    }
                )
            }
        }

        val calisanVideoUrl = videoUrl ?: run {
            Log.e(name, "Video linki çıkarılamadı, sadece altyazılar eklendi")
            return
        }

        callback.invoke(
            newExtractorLink(
                source = name,
                name = name,
                url = calisanVideoUrl,
                type = ExtractorLinkType.M3U8
            ) {
                this.referer = url
                this.quality = Qualities.Unknown.value
                this.headers = mapOf(
                    "Accept" to "*/*",
                    "Origin" to "https://closeload.filmmakinesi.to"
                )
            }
        )
        Log.d(name, "ExtractorLink eklendi: $calisanVideoUrl")
    }

    /** Sayfadaki olası video adreslerini (şifre çözücüler + JSON-LD) sırayla döndürür. */
    private fun adaylariCoz(rawHtml: String, istekUrl: String): List<String> {
        val sonuc = mutableListOf<String>()
        val alanAdi = Regex("""https?://([^/]+)""").find(istekUrl)?.groupValues?.get(1)
            ?: "closeload.filmmakinesi.to"

        // 1) Yeni şifreleme: c78("parça|parça|...".split("|"))
        Regex("""(\w+)\s*=\s*c78\(\s*"([^"]+)"\s*\.split\(\s*["']\|["']\s*\)\s*\)""")
            .find(rawHtml)?.let { eslesme ->
                val parcalar = eslesme.groupValues[2].split("|")
                Log.d(name, "c78 biçimi: ${parcalar.size} parça, alan adı: $alanAdi")
                decodeC78(parcalar, alanAdi)?.takeIf { it.startsWith("http") }?.let(sonuc::add)
            }

        // 2) Eski şifreleme: var X = func(["...", "..."])
        val varPattern = Regex("""var\s+(\w+)\s*=\s*(\w+)\s*\(\s*\[(.*?)\]\s*\)""", RegexOption.DOT_MATCHES_ALL)
        varPattern.find(rawHtml)?.let { eslesme ->
            val funcName = eslesme.groupValues[2]
            val parts = Regex(""""([^"]*)"""").findAll(eslesme.groupValues[3]).map {
                it.groupValues[1].replace("\\/", "/").replace("\\\"", "\"")
            }.toList()

            val funcBody = extractFuncBody(rawHtml, funcName)
            val cozulen = if (funcBody != null) {
                parseAndExecuteJs(funcBody, parts)
            } else {
                Log.w(name, "Fonksiyon gövdesi yok ($funcName), bilinen çözücüler denenecek")
                tryAllDecryptors(parts)
            }
            if (cozulen != null) sonuc.add(cozulen)
            // Fonksiyon gövdesi okunamadıysa/yanlış çözdüyse hazır çözücüleri de dene
            tryAllDecryptors(parts)?.let(sonuc::add)
        }

        // 3) JSON-LD: master.txt de geçerli bir HLS manifesti olabilir
        Regex(""""contentUrl"\s*:\s*"([^"]+)"""").find(rawHtml)
            ?.groupValues?.get(1)?.let(sonuc::add)

        return sonuc.filter { it.startsWith("http") }.distinct()
    }
    /**
     * Yeni CloseLoad şifrelemesi: `c78("parça|parça|...".split("|"))` çağrısının çözümü.
     *
     * Parça listesinden iki eleman çıkarılır; biri alan adıyla birleşip tohum olur.
     * Tohumdan üretilen sayılarla harf kaydırma, ters çevirme, karıştırma ve XOR uygulanır.
     */
    private fun decodeC78(parts: List<String>, alanAdi: String): String? {
        if (parts.size < 20) return null
        val dizi = parts.toMutableList()

        val uzunluk = dizi.size - 2
        val idxTohum = uzunluk % 7
        val idxOp = 8 + (uzunluk % 5)
        if (idxOp >= dizi.size) return null

        val opStr = dizi.removeAt(idxOp)
        if (idxTohum >= dizi.size) return null
        val tohum = dizi.removeAt(idxTohum) + alanAdi
        var veri = dizi.joinToString("")

        if (tohum.length > 4096) veri = atob(veri)

        var h1 = 0
        var h2 = 0
        tohum.forEachIndexed { i, c ->
            val deger = c.code
            h1 = (h1 * 37 + deger) % 241
            h2 = (h2 + ((deger shl 1) xor i)) and 255
        }

        val baslangic = (h1 * 3 + h2) % 256
        val adim = (h2 % 11) + 5
        var tohum2 = ((h2 * 251 + h1) % 65519) + 1

        for (i in opStr.indices.reversed()) {
            when (val ch = opStr[i]) {
                '7' -> veri = atob(veri)
                '3' -> veri = veri.reversed()
                else -> {
                    val kaydirma = (26 - ((ch.code - 96) % 26)) % 26
                    veri = caesarShift(veri, kaydirma)
                }
            }
        }
        if (opStr.length > 2048) veri = veri.reversed()

        val n = veri.length
        val perm = IntArray(n)
        for (i in n - 1 downTo 1) {
            tohum2 = (tohum2 * 97 + 41) % 65519
            perm[i] = tohum2 % (i + 1)
        }

        val harfler = veri.toCharArray()
        for (i in 1 until n) {
            val j = perm[i]
            val gecici = harfler[i]
            harfler[i] = harfler[j]
            harfler[j] = gecici
        }

        var acc = baslangic
        val sb = StringBuilder(n)
        for (ch in harfler) {
            val deger = ch.code
            acc = (acc * 5 + adim) % 256
            sb.append((deger xor acc).toChar())
            acc = (acc + deger) % 256
        }

        return sb.toString().trim().takeIf { it.isNotBlank() }
    }

    private fun extractFuncBody(rawHtml: String, funcName: String): String? {
        val startIdx = rawHtml.indexOf("function $funcName")
        if (startIdx == -1) return null

        val braceIdx = rawHtml.indexOf('{', startIdx)
        if (braceIdx == -1) return null

        var braceCount = 1
        var i = braceIdx + 1
        while (braceCount > 0 && i < rawHtml.length) {
            when (rawHtml[i]) {
                '{' -> braceCount++
                '}' -> braceCount--
            }
            i++
        }
        return if (braceCount == 0) rawHtml.substring(braceIdx + 1, i - 1) else null
    }
    private fun parseAndExecuteJs(funcBody: String, parts: List<String>): String? {
        return try {
            val seedMatch = Regex(
                """var\s+(\w+)\s*=\s*"([^"]+)"\s*;\s*var\s+(\w+)\s*=\s*"([^"]+)""""
            ).find(funcBody) ?: run {
                Log.w(name, "Seed/ops string'leri bulunamadı")
                return null
            }
            val seedStr = seedMatch.groupValues[2]
            val opsStr = seedMatch.groupValues[4]
            Log.d(name, "Seed: '$seedStr', Ops: '$opsStr'")

            var la8q = parts.joinToString("")
            var m1l = 0
            var rfdgf = 0
            for (i in seedStr.indices) {
                val ioz = seedStr[i].code
                m1l = (m1l * 31 + ioz) % 251
                rfdgf = (rfdgf xor (ioz + i)) and 255
            }
            val ucv = (m1l + rfdgf) % 256
            val h52gx = (m1l % 13) + 3
            var ws7g = ((m1l * 256 + rfdgf) % 65521) + 1
            Log.d(name, "ucv=$ucv, h52gx=$h52gx, ws7g=$ws7g")
            for (i in opsStr.length - 1 downTo 0) {
                val ch = opsStr[i]
                la8q = when (ch) {
                    'b' -> atob(la8q)
                    'v' -> la8q.reversed()
                    else -> {
                        val tcxa = (26 - ((ch.code - 64) % 26)) % 26
                        caesarShift(la8q, tcxa)
                    }
                }
            }
            if (opsStr.length > 4096) la8q = la8q.reversed()

            Log.d(name, "Operasyonlar sonrası uzunluk: ${la8q.length}")
            val tmzq = la8q.length
            if (tmzq > 1) {
                val gtwld = IntArray(tmzq)
                for (xfm8 in tmzq - 1 downTo 1) {
                    ws7g = (ws7g * 75 + 74) % 65537
                    gtwld[xfm8] = ws7g % (xfm8 + 1)
                }
                val onw = la8q.toCharArray()
                for (xfm8 in 1 until tmzq) {
                    val j = gtwld[xfm8]
                    val tmp = onw[xfm8]
                    onw[xfm8] = onw[j]
                    onw[j] = tmp
                }
                la8q = String(onw)
            }
            val sb = StringBuilder(tmzq)
            var ew0 = ucv
            for (c in la8q) {
                val ioz = c.code
                ew0 = (ew0 + h52gx) % 256
                sb.append((ioz xor ew0).toChar())
                ew0 = (ew0 + ioz) % 256
            }

            val result = sb.toString()
            Log.d(name, "Çözülen değer: ${result.take(200)}")
            result.trim().takeIf { it.startsWith("http") }
        } catch (e: Exception) {
            Log.e(name, "JS Parser hatası: ${e.message}")
            null
        }
    }

    private fun atob(s: String): String {
        var str = s.trim()
        val padding = 4 - str.length % 4
        if (padding != 4) str += "=".repeat(padding)
        return Base64.decode(str, Base64.DEFAULT).toString(Charsets.ISO_8859_1)
    }

    private fun btoa(s: String): String {
        return Base64.encodeToString(s.toByteArray(Charsets.ISO_8859_1), Base64.DEFAULT).trim()
    }

    private fun caesarShift(text: String, shift: Int): String {
        return text.map { c ->
            when {
                c in 'A'..'Z' -> ((c.code - 'A'.code + shift) % 26 + 'A'.code).toChar()
                c in 'a'..'z' -> ((c.code - 'a'.code + shift) % 26 + 'a'.code).toChar()
                else -> c
            }
        }.joinToString("")
    }

    private fun xorUnmix(text: String, accStart: Int, increment: Int): String {
        var acc = accStart
        val unmix = StringBuilder()
        for (i in text.indices) {
            val b = text[i].code
            acc = (acc + increment) % 256
            val plain = b xor acc
            acc = (acc + b) % 256
            unmix.append(plain.toChar())
        }
        return unmix.toString()
    }
    private fun tryAllDecryptors(parts: List<String>): String? {
        val decryptors = listOf(::decryptV1, ::decryptV2, ::decryptV3, ::decryptV4)
        for ((index, decryptor) in decryptors.withIndex()) {
            try {
                val result = decryptor(parts)
                if (!result.isNullOrBlank() && result.contains("http")) {
                    Log.d(name, "Fallback decryptor v${index + 1} başarılı!")
                    return result
                }
            } catch (e: Exception) {
                Log.d(name, "Fallback decryptor v${index + 1} başarısız: ${e.message}")
            }
        }
        return null
    }

    private fun decryptV1(valueParts: List<String>): String? {
        var value = valueParts.joinToString("")
        value = caesarShift(value, 9); value = caesarShift(value, 16)
        value = value.reversed()
        var decoded = atob(value); decoded = atob(decoded)
        return xorUnmix(decoded, 241, 11)
    }

    private fun decryptV2(valueParts: List<String>): String? {
        var value = valueParts.joinToString("")
        value = value.reversed(); value = caesarShift(value, 15)
        var decoded = atob(value); decoded = decoded.reversed(); decoded = atob(decoded)
        return xorUnmix(decoded, 185, 12)
    }

    private fun decryptV3(valueParts: List<String>): String? {
        var value = valueParts.joinToString("")
        var decoded = atob(value); decoded = atob(decoded)
        decoded = decoded.reversed(); decoded = caesarShift(decoded, 25); decoded = atob(decoded)
        return xorUnmix(decoded, 77, 9)
    }

    private fun decryptV4(valueParts: List<String>): String? {
        var value = valueParts.joinToString("")
        var decoded = atob(value); decoded = decoded.reversed(); decoded = atob(decoded)
        return xorUnmix(decoded, 130, 10)
    }
}