package com.keyiflerolsun

import android.util.Log
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.network.CloudflareKiller
import com.lagradost.cloudstream3.newSubtitleFile
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.fixUrl
import com.lagradost.cloudstream3.utils.newExtractorLink

/**
 * Dizipal oynatıcısı.
 *
 * Oynatıcı iki farklı biçimde karşımıza çıkabiliyor:
 *  1) Yeni sürüm: sayfada `window.NP_CONFIG = { sources: [ { master: "https://.../nm.php?v=..." } ] }`
 *     ve `master` adresi doğrudan HLS (m3u8) döndürür.
 *  2) Eski sürüm: `window.openPlayer("<id>")` + `/source2.php?v=<id>` içinde `"file":"..."`.
 *
 * İkisi de destekleniyor; ilki bulunamazsa ikincisine düşülür.
 */
class DizipalPlayer : ExtractorApi() {
    override var name = "DizipalPlayer"
    override var mainUrl = "dplayer82.site"
    override val requiresReferer = true

    private val cloudflareKiller by lazy { CloudflareKiller() }

    private val userAgent =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val defaultHeaders = mapOf(
            "User-Agent" to userAgent,
            "Referer" to (referer ?: "https://dizipal1592.com/"),
            "Accept-Language" to "tr-TR,tr;q=0.9,en;q=0.8"
        )

        val response = app.get(
            url,
            referer = referer,
            headers = defaultHeaders,
            interceptor = cloudflareKiller
        ).text

        val origin = Regex("""https?://[^/]+""").find(url)?.value ?: "https://$mainUrl"

        // --- Altyazılar (eski biçim: "file":"...","label":"..." ) ---
        val subUrls = mutableSetOf<String>()
        Regex(""""file":"((?:\\"|[^"])+)","label":"((?:\\"|[^"])+)"""").findAll(response).forEach {
            val (subUrlExt, subLangExt) = it.destructured

            val subUrl = subUrlExt.replace("\\/", "/").replace("\\u0026", "&").replace("\\", "")
            val subLang = subLangExt.replace("\\u0131", "ı").replace("\\u0130", "İ")
                .replace("\\u00fc", "ü").replace("\\u00e7", "ç")
                .replace("\\u011f", "ğ").replace("\\u015f", "ş")

            if (!subUrls.add(subUrl)) return@forEach

            subtitleCallback.invoke(
                newSubtitleFile(lang = subLang, url = fixUrl(subUrl)) {
                    headers = mapOf("Referer" to url, "User-Agent" to userAgent)
                }
            )
        }

        // --- 1) Yeni oynatıcı: NP_CONFIG içindeki "master" adresi doğrudan m3u8 ---
        var linkEklendi = false
        Regex(""""master"\s*:\s*"([^"]+)"""").findAll(response).forEach { match ->
            val fileUrl = duzeltUrl(match.groupValues[1])
            Log.d("DiziPal", "--> NP_CONFIG master: $fileUrl")
            linkEkle(fileUrl, origin, url, callback)
            linkEklendi = true
        }

        // --- 2) Eski oynatıcı: window.openPlayer(id) + source2.php ---
        if (!linkEklendi) {
            val playlistId = Regex("""window\.openPlayer\s*\(\s*['"]([^'"]+)['"]""")
                .find(response)?.groupValues?.get(1)

            Log.d("DiziPal", "--> playlistId: $playlistId")
            if (playlistId != null) {
                val apiUrl = "$origin/source2.php?v=$playlistId"
                Log.d("DiziPal", "--> apiUrl: $apiUrl")
                val apiResponse = app.get(
                    apiUrl,
                    referer = url,
                    headers = defaultHeaders,
                    interceptor = cloudflareKiller
                ).text

                Regex(""""file"\s*:\s*"([^"]+)"""").findAll(apiResponse).forEach { m ->
                    val fileUrl = duzeltUrl(m.groupValues[1])
                    Log.d("DiziPal", "--> source2 file: $fileUrl")
                    linkEkle(fileUrl, origin, url, callback)
                    linkEklendi = true
                }
            }
        }

        if (!linkEklendi) {
            Log.e("DiziPal", "--> Oynatıcıdan video adresi çıkarılamadı: $url")
        }
    }

    private suspend fun linkEkle(
        hamUrl: String,
        origin: String,
        referer: String,
        callback: (ExtractorLink) -> Unit
    ) {
        if (hamUrl.isBlank()) return

        // Eski biçimde parça adresi ".../m.php" olarak geliyor -> HLS master'a çevir.
        // Dikkat: yeni oynatıcıdaki "nm.php" buna benzemez, dokunulmamalı.
        val fileUrl = if (hamUrl.contains(Regex("""/m\.php(?=$|\?)"""))) {
            hamUrl.replace("/m.php", "/master.m3u8")
        } else {
            hamUrl
        }

        callback.invoke(
            newExtractorLink(
                source = name,
                name = "DPlayer",
                url = fileUrl,
                type = ExtractorLinkType.M3U8
            ) {
                this.referer = referer
                this.headers = mapOf("Origin" to origin, "Referer" to referer)
                this.quality = Qualities.Unknown.value
            }
        )
    }

    private fun duzeltUrl(raw: String): String {
        var u = raw.replace("\\/", "/").replace("\\u0026", "&").replace("\\", "")
        if (u.startsWith("//")) u = "https:$u"
        if (u.startsWith("://")) u = "https$u"
        if (!u.startsWith("http")) u = "https://$u"
        return u
    }
}
