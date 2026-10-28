package com.keyiflerolsun

import android.util.Log
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.newSubtitleFile
import java.net.URLEncoder

/**
 * Kaynağın kendi altyazısı yoksa OpenSubtitles üzerinden altyazı çeker.
 *
 * - API anahtarı / üyelik gerekmez (Stremio'nun herkese açık OpenSubtitles v3 aynası kullanılır).
 * - Film/dizi adı verilirse Cinemeta üzerinden IMDb kimliği bulunur.
 * - Öncelik Türkçe, sonra İngilizce; hiçbiri yoksa ilk bulunan 2 altyazı eklenir.
 *
 * Not: Bu dosya birden fazla eklentide birebir aynıdır (her eklenti kendi .cs3'üne derleniyor).
 */
object OrtakAltyazi {
    private const val OS_TABAN = "https://opensubtitles-v3.strem.io"
    private const val CINEMETA = "https://v3-cinemeta.strem.io"
    private const val ETIKET = "OrtakAltyazi"

    private val dilAdlari = mapOf(
        "tur" to "Türkçe",
        "eng" to "İngilizce",
        "ger" to "Almanca",
        "fre" to "Fransızca",
        "spa" to "İspanyolca",
        "ita" to "İtalyanca",
        "rus" to "Rusça",
        "ara" to "Arapça",
        "dut" to "Felemenkçe",
        "por" to "Portekizce",
        "pol" to "Lehçe",
        "gre" to "Yunanca",
        "kor" to "Korece",
        "jpn" to "Japonca",
        "chi" to "Çince",
        "zho" to "Çince",
        "hin" to "Hintçe",
        "per" to "Farsça",
        "ukr" to "Ukraynaca",
        "swe" to "İsveççe",
        "nor" to "Norveççe",
        "dan" to "Danca",
        "fin" to "Fince",
        "cze" to "Çekçe",
        "hun" to "Macarca",
        "rum" to "Romence",
        "bul" to "Bulgarca",
        "srp" to "Sırpça",
        "hrv" to "Hırvatça",
        "alb" to "Arnavutça",
        "heb" to "İbranice",
        "tha" to "Tayca",
        "vie" to "Vietnamca",
        "ind" to "Endonezce",
        "may" to "Malayca"
    )

    /** En fazla kaç altyazı eklenecek (dil başına). */
    private const val TR_LIMIT = 3
    private const val EN_LIMIT = 2

    private data class Aday(val dil: String, val url: String, val dosya: String)

    /**
     * @param imdbId  "tt1234567" biçiminde IMDb kimliği (varsa en isabetlisi)
     * @param baslik  Film/dizi adı (IMDb kimliği yoksa arama için)
     * @param yil     Yapım yılı (başlık aramasında eşleşmeyi iyileştirir)
     * @param sezon   Dizi ise sezon numarası
     * @param bolum   Dizi ise bölüm numarası
     * @return eklenen altyazı sayısı
     */
    suspend fun ekle(
        imdbId: String?,
        baslik: String?,
        yil: Int? = null,
        sezon: Int? = null,
        bolum: Int? = null,
        subtitleCallback: (SubtitleFile) -> Unit
    ): Int {
        val diziMi = sezon != null && bolum != null
        val id = imdbId?.takeIf { it.startsWith("tt") }
            ?: baslik?.takeIf { it.isNotBlank() }?.let { imdbBul(it, yil, diziMi) }

        if (id == null) {
            Log.d(ETIKET, "IMDb kimliği bulunamadı, altyazı aranamadı (başlık: $baslik)")
            return 0
        }

        val adres = if (diziMi) {
            "$OS_TABAN/subtitles/series/$id:$sezon:$bolum.json"
        } else {
            "$OS_TABAN/subtitles/movie/$id.json"
        }

        val govde = try {
            app.get(adres).text
        } catch (e: Exception) {
            Log.w(ETIKET, "Altyazı listesi alınamadı: ${e.message}")
            return 0
        }

        val adaylar = Regex("""\{[^{}]*\}""").findAll(govde).mapNotNull { eslesme ->
            val parca = eslesme.value
            val url = Regex(""""url"\s*:\s*"([^"]+)"""").find(parca)?.groupValues?.get(1)
                ?: return@mapNotNull null
            val dil = Regex(""""lang"\s*:\s*"([^"]+)"""").find(parca)?.groupValues?.get(1)
                ?: return@mapNotNull null
            val dosya = Regex(""""subtitleFileName"\s*:\s*"([^"]*)"""")
                .find(parca)?.groupValues?.get(1) ?: ""
            Aday(dil.lowercase(), url, dosya)
        }.toList()

        if (adaylar.isEmpty()) {
            Log.d(ETIKET, "Altyazı bulunamadı ($id)")
            return 0
        }

        var eklenen = 0
        val secilenler = mutableListOf<Aday>()

        val turkce = adaylar.filter { it.dil == "tur" }
        val ingilizce = adaylar.filter { it.dil == "eng" }

        if (turkce.isNotEmpty() || ingilizce.isNotEmpty()) {
            secilenler += turkce.distinctBy { it.url }.take(TR_LIMIT)
            secilenler += ingilizce.distinctBy { it.url }.take(EN_LIMIT)
        } else {
            // Türkçe/İngilizce yoksa elde ne varsa (en fazla 2) göster
            secilenler += adaylar.distinctBy { it.url }.take(2)
        }

        for (aday in secilenler) {
            val ad = dilAdlari[aday.dil] ?: aday.dil
            subtitleCallback.invoke(
                newSubtitleFile(lang = ad, url = aday.url) {
                    headers = mapOf("User-Agent" to "Mozilla/5.0")
                }
            )
            eklenen++
        }

        Log.d(ETIKET, "$id için $eklenen altyazı eklendi (toplam aday: ${adaylar.size})")
        return eklenen
    }

    /** Başlık + yıl ile IMDb kimliği bulur (Cinemeta). */
    private suspend fun imdbBul(baslik: String, yil: Int?, diziMi: Boolean): String? {
        val tur = if (diziMi) "series" else "movie"
        val sorgu = URLEncoder.encode(baslik, "UTF-8")
        val govde = try {
            app.get("$CINEMETA/catalog/$tur/top/search=$sorgu.json").text
        } catch (e: Exception) {
            Log.w(ETIKET, "Başlık araması başarısız: ${e.message}")
            return null
        }

        val parcalar = govde.split("""{"id":"""").drop(1)
        var ilkEslesme: String? = null

        for (parca in parcalar) {
            val imdb = Regex(""""imdb_id"\s*:\s*"(tt\d+)"""")
                .find(parca)?.groupValues?.get(1) ?: continue
            if (ilkEslesme == null) ilkEslesme = imdb

            val yilMetni = Regex(""""releaseInfo"\s*:\s*"(\d{4})"""")
                .find(parca)?.groupValues?.get(1)?.toIntOrNull()
            if (yil != null && yilMetni == yil) return imdb
        }

        return ilkEslesme
    }
}
