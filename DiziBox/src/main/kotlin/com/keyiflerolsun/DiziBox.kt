package com.keyiflerolsun

import android.util.Base64
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.network.CloudflareKiller
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import com.lagradost.cloudstream3.utils.StringUtils.decodeUri
import okhttp3.Interceptor
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class DiziBox : MainAPI() {
    override var mainUrl              = "https://www.dizibox.live"
    override var name                 = "DiziBox"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = true
    override val supportedTypes       = setOf(TvType.TvSeries)

    override var sequentialMainPage            = false
    override var sequentialMainPageDelay       = 0L
    override var sequentialMainPageScrollDelay = 0L

    private val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36"

    private val baseCookies = mapOf(
        "LockUser"      to "true",
        "isTrustedUser" to "true",
        "dbxu"          to "1744054959089"
    )

    private val cloudflareKiller by lazy { CloudflareKiller() }
    private val interceptor      by lazy { CloudflareInterceptor(cloudflareKiller, userAgent) }

    init {
        MolyStreamProxy.patchDnsForLocalhost()
    }

    class CloudflareInterceptor(
        private val cloudflareKiller: CloudflareKiller,
        private val userAgent: String
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder()
                .header("User-Agent", userAgent)
                .build()
            val response = chain.proceed(request)
            if (response.code !in listOf(403, 503, 429) && response.header("cf-mitigated") == null) {
                return response
            }
            val bodySample = runCatching { response.peekBody(4096).string() }.getOrDefault("")
            if (
                bodySample.contains("Güvenlik taramasından geçiriliyorsunuz")
                || bodySample.contains("cf-browser-verification")
                || bodySample.contains("Checking your browser")
                || bodySample.contains("just a moment", ignoreCase = true)
                || response.code in listOf(403, 503, 429)
            ) {
                response.close()
                return cloudflareKiller.intercept(chain)
            }
            return response
        }
    }

    override val mainPage = mainPageOf(
        "${mainUrl}/tum-bolumler/page/SAYFA/"                        to "Son Bölümler",
        "${mainUrl}/tum-bolumler/page/SAYFA/?tip=populer"            to "Popüler Diziler",
        "${mainUrl}/dizi-arsivi/page/SAYFA/"                         to "Yeni Eklenenler",
        "${mainUrl}/dizi-arsivi/page/SAYFA/?tur[0]=aksiyon&yil&imdb" to "Aksiyon & Macera"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val rawUrl = request.data
        val url = if (page == 1) {
            if (rawUrl.contains("?")) {
                rawUrl.substringBefore("/page/SAYFA/") + "/?" + rawUrl.substringAfter("?")
            } else {
                rawUrl.substringBefore("/page/SAYFA/") + "/"
            }
        } else {
            rawUrl.replace("SAYFA", "$page")
        }

        val document = app.get(
            url,
            cookies     = baseCookies,
            interceptor = interceptor,
            cacheTime   = 60
        ).document

        val home = document.select("article.detailed-article, article.article-episode-card a.figure-link")
            .mapNotNull { it.toMainPageResult() }

        val isHorizontal = request.name.contains("Bölümler") || request.name.contains("Popüler")
        return newHomePageResponse(listOf(HomePageList(request.name, home, isHorizontal)), home.isNotEmpty())
    }

    private fun Element.toMainPageResult(): SearchResponse? {
        val titleEl = this.selectFirst("h3 a")
        val title   = titleEl?.text() ?: this.selectFirst("img")?.attr("alt") ?: return null
        val imgEl   = this.selectFirst("img")
        val rawSrc  = imgEl?.attr("data-src")?.takeIf { it.isNotBlank() }
            ?: imgEl?.attr("data-lazy-src")?.takeIf { it.isNotBlank() }
            ?: imgEl?.attr("data-original")?.takeIf { it.isNotBlank() }
            ?: imgEl?.attr("src")?.takeIf { it.isNotBlank() && !it.startsWith("data:") }
        val imgUrl  = fixUrlNull(rawSrc)
        val href    = fixUrlNull(titleEl?.attr("href") ?: this.attr("href")) ?: return null
        val rating  = this.selectFirst("span.label-imdb b")?.text()?.trim()

        return newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
            this.posterUrl = imgUrl
            this.posterHeaders = mapOf(
                "User-Agent" to USER_AGENT,
                "Referer"    to "$mainUrl/"
            )
            this.score     = Score.from10(rating)
        }
    }

    data class SearchApiResponse(@JsonProperty("results") val results: List<SearchResult> = emptyList())
    data class SearchResult(
        @JsonProperty("post_title")          val postTitle           : String  = "",
        @JsonProperty("permalink")           val permalink           : String  = "",
        @JsonProperty("attachment_thumbnail") val attachmentThumbnail: String? = null
    )

    override suspend fun search(query: String): List<SearchResponse> {
        val url      = "$mainUrl/wp-admin/admin-ajax.php?s=$query&action=dwls_search"
        val response = app.get(
            url,
            headers     = mapOf(
                "X-Requested-With" to "XMLHttpRequest",
                "Referer"          to "$mainUrl/?s=$query"
            ),
            cookies     = baseCookies,
            interceptor = interceptor
        )
        if (!response.isSuccessful) return emptyList()

        val json = tryParseJson<SearchApiResponse>(response.text) ?: return emptyList()
        return json.results.mapNotNull { result ->
            val thumbnail = result.attachmentThumbnail?.replace("50x50", "200x290")
            newTvSeriesSearchResponse(result.postTitle, result.permalink, TvType.TvSeries) {
                this.posterUrl = fixUrlNull(thumbnail)
            }
        }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse? {
        var document = app.get(
            url,
            cookies     = baseCookies,
            interceptor = interceptor
        ).document

        val archiveLink = document.selectFirst("div#archive-box a.archive-title")?.attr("href")
        if (!archiveLink.isNullOrBlank()) {
            val redirectUrl = fixUrlNull(archiveLink) ?: url
            document = app.get(
                redirectUrl,
                cookies     = baseCookies,
                interceptor = interceptor
            ).document
        }

        val title    = document.selectFirst("div.tv-overview h1 a")?.text()?.trim() ?: return null
        val posterEl = document.selectFirst("div.tv-overview figure img")
        val rawPoster = posterEl?.attr("data-src")?.takeIf { it.isNotBlank() }
            ?: posterEl?.attr("data-lazy-src")?.takeIf { it.isNotBlank() }
            ?: posterEl?.attr("data-original")?.takeIf { it.isNotBlank() }
            ?: posterEl?.attr("src")?.takeIf { it.isNotBlank() && !it.startsWith("data:") }
        val poster   = fixUrlNull(rawPoster)
        val plot     = document.selectFirst("div.tv-story p")?.text()?.trim()
        val year     = document.selectFirst("a[href*='/yil/']")?.text()?.trim()?.toIntOrNull()
        val tags     = document.select("a[href*='/tur/']").map { it.text() }
        val actors   = document.select("a[href*='/oyuncu/']").map { Actor(it.text()) }
        val trailer  = document.selectFirst("div.tv-overview iframe")?.attr("src")
        val rating   = document.selectFirst("span.label-imdb b")?.text()?.trim()

        val seasonLinks = document.select("div#seasons-list a").mapNotNull {
            fixUrlNull(it.attr("href"))
        }

        val allSeasonDocs = seasonLinks.amap { seasonUrl ->
            try {
                app.get(
                    seasonUrl,
                    cookies     = baseCookies,
                    interceptor = interceptor,
                    cacheTime   = 120
                ).document
            } catch (_: Throwable) {
                null
            }
        }.filterNotNull()

        val episodeList = mutableListOf<Episode>()
        allSeasonDocs.forEach { seasonDoc ->

            seasonDoc.select("article.grid-box").forEach epLoop@{ epElem ->
                val epTitleEl = epElem.selectFirst("div.post-title a.season-episode") ?: return@epLoop
                val epRawTitle = epTitleEl.text().trim()
                val epHref     = fixUrlNull(epTitleEl.attr("href")) ?: return@epLoop

                val dateEl   = epElem.selectFirst("small.date")
                val dateText = dateEl?.text()?.trim()?.let { d ->
                    d.replace("Ocak", "01").replace("Şubat", "02").replace("Mart", "03")
                     .replace("Nisan", "04").replace("Mayıs", "05").replace("Haziran", "06")
                     .replace("Temmuz", "07").replace("Ağustos", "08").replace("Eylül", "09")
                     .replace("Ekim", "10").replace("Kasım", "11").replace("Aralık", "12")
                }

                val sNum = Regex("""(\d+)\.? ?Sezon""").find(epRawTitle)?.groupValues?.get(1)?.toIntOrNull() ?: 1
                val eNum = Regex("""(\d+)\.? ?Bölüm""").find(epRawTitle)?.groupValues?.get(1)?.toIntOrNull()

                episodeList.add(newEpisode(epHref) {
                    this.name      = "Bölüm"
                    this.season    = sNum
                    this.episode   = eNum
                    this.posterUrl = poster
                    if (dateText != null) {
                        val parts = dateText.split(" ")
                        if (parts.size >= 3) {
                            val formatted = "${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}"
                            addDate(formatted)
                        }
                    }
                })
            }
        }

        val sortedEpisodes = episodeList.sortedWith(compareBy({ it.season }, { it.episode }))

        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, sortedEpisodes) {
            this.posterUrl = poster
            this.posterHeaders = mapOf(
                "User-Agent" to USER_AGENT,
                "Referer"    to "$mainUrl/"
            )
            this.plot      = plot
            this.year      = year
            this.tags      = tags
            this.score     = Score.from10(rating)
            addActors(actors)
            addTrailer(trailer)
        }
    }

    override suspend fun loadLinks(
        data             : String,
        isCasting        : Boolean,
        subtitleCallback : (SubtitleFile) -> Unit,
        callback         : (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(
            data,
            cookies     = baseCookies,
            interceptor = interceptor
        ).document

        val iframeUrl = document.selectFirst("div#video-area iframe")?.attr("src")

        val altOptions = document.select("div.video-toolbar option[value]")
            .mapNotNull { fixUrlNull(it.attr("value")) }
            .filter { it != data }
            .take(3)

        // Alternatif oynatıcıları (VidMoly vb.) önce tara ki doğrudan çalışan 1080p yayın ilk sırada açılsın
        altOptions.amap { altUrl ->
            try {
                val sourceDoc = app.get(
                    altUrl,
                    cookies     = baseCookies,
                    interceptor = interceptor,
                    cacheTime   = 60
                ).document

                val srcIframe = sourceDoc.selectFirst("div#video-area iframe")?.attr("src")
                if (srcIframe != null) {
                    processIframe(srcIframe, altUrl, data, subtitleCallback, callback)
                }
            } catch (_: Throwable) {}
        }

        if (iframeUrl != null) {
            processIframe(iframeUrl, data, data, subtitleCallback, callback)
        }

        return true
    }

    private suspend fun processIframe(
        iframeUrl        : String,
        sourceUrl        : String,
        referer          : String,
        subtitleCallback : (SubtitleFile) -> Unit,
        callback         : (ExtractorLink) -> Unit
    ) {
        when {
            iframeUrl.contains("/player/moly/moly.php") || iframeUrl.contains("/player/haydi.php") -> {
                val molyUrl = when {
                    iframeUrl.contains("moly.php") -> iframeUrl.replace("moly.php?h=", "moly.php?wmode=opaque&h=")
                    else                           -> iframeUrl.replace("haydi.php?v=", "haydi.php?wmode=opaque&v=")
                }
                var subDoc = app.get(
                    molyUrl,
                    referer     = referer,
                    cookies     = baseCookies,
                    interceptor = interceptor
                ).document

                val atobData = Regex("""unescape\("(.*)"\)""").find(subDoc.html())?.groupValues?.get(1)
                if (atobData != null) {
                    val decoded = String(Base64.decode(atobData.decodeUri(), Base64.DEFAULT), Charsets.UTF_8)
                    subDoc = Jsoup.parse(decoded)
                }

                val subFrame = subDoc.selectFirst("div#Player iframe")?.attr("src") ?: return
                if (subFrame.contains("vidmoly") || subFrame.contains("moly")) {
                    if (tryExtractDirect(subFrame, "$mainUrl/", subtitleCallback, callback)) return
                }
                if (!subFrame.contains("ok.ru")) {
                    loadExtractor(subFrame, "$mainUrl/", subtitleCallback, callback)
                }
            }

            iframeUrl.contains("/player/king/king.php") || iframeUrl.contains("php?v=") -> {
                val playerUrl = if (iframeUrl.contains("king.php")) iframeUrl else iframeUrl.replace("php?v=", "php?wmode=opaque&v=")
                val playerDoc = app.get(
                    playerUrl,
                    referer     = sourceUrl,
                    headers     = mapOf(
                        "User-Agent"     to USER_AGENT,
                        "Referer"        to sourceUrl,
                        "Sec-Fetch-Dest" to "iframe",
                        "Sec-Fetch-Mode" to "navigate",
                        "Sec-Fetch-Site" to "same-origin"
                    ),
                    cookies     = baseCookies,
                    interceptor = interceptor
                ).document

                val finalEmbed = playerDoc.selectFirst("div#Player iframe")?.attr("src")
                    ?: return

                if (finalEmbed.contains("dbx.molystream")) {
                    // Altyazı varsa şifreli sayfadan çıkar
                    runCatching {
                        val iDoc      = app.get(finalEmbed, referer = "$mainUrl/", interceptor = interceptor).text
                        val cryptData = Regex("""CryptoJS\.AES\.decrypt\("(.*?)",""").find(iDoc)?.groupValues?.get(1)
                        val cryptPass = Regex("""","(.*?)"\);""").find(iDoc)?.groupValues?.get(1)
                        if (cryptData != null && cryptPass != null) {
                            val decrypted = CryptoJS.decrypt(cryptPass, cryptData)
                            Regex(""""file"\s*:\s*"(https?://[^"]+\.(?:vtt|srt)[^"]*)"\s*,\s*"label"\s*:\s*"([^"]+)"""").findAll(decrypted).forEach { m ->
                                subtitleCallback.invoke(newSubtitleFile(m.groupValues[2], m.groupValues[1]))
                            }
                        }
                    }

                    val sheila   = finalEmbed.replace("/embed/", "/embed/sheila/")
                    val m3u8Data = app.get(
                        sheila,
                        referer     = finalEmbed,
                        headers     = mapOf(
                            "User-Agent"     to USER_AGENT,
                            "Referer"        to finalEmbed,
                            "Sec-Fetch-Dest" to "empty",
                            "Sec-Fetch-Mode" to "cors"
                        ),
                        interceptor = interceptor
                    ).text
                    val m3u8Url = m3u8Data.lineSequence().firstOrNull { it.trim().startsWith("http") }?.trim()
                    if (m3u8Url != null) {
                        val proxiedUrl = MolyStreamProxy.proxyPlaylist(m3u8Url, finalEmbed)
                        callback.invoke(
                            newExtractorLink(
                                source = name,
                                name   = "King / MolyStream (1080p)",
                                url    = proxiedUrl,
                                type   = ExtractorLinkType.M3U8
                            ) {
                                this.quality = Qualities.P1080.value
                            }
                        )
                        return
                    }
                } else {
                    val cleanEmbed = finalEmbed.replace("vidmoly.me", "vidmoly.biz").replace("vidmoly.to", "vidmoly.biz")
                    if (cleanEmbed.contains("vidmoly") || cleanEmbed.contains("moly")) {
                        if (tryExtractDirect(cleanEmbed, sourceUrl, subtitleCallback, callback)) return
                    }
                    loadExtractor(cleanEmbed, sourceUrl, subtitleCallback, callback)
                }
            }

            else -> {
                loadExtractor(iframeUrl, sourceUrl, subtitleCallback, callback)
            }
        }
    }

    private suspend fun tryExtractDirect(
        embedUrl: String,
        referer: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        try {
            val fixedUrl = httpsify(embedUrl)
                .replace("vidmoly.me", "vidmoly.biz")
                .replace("vidmoly.to", "vidmoly.biz")
                .let { if (it.contains("/w/")) it.replaceFirst("/w/", "/embed-") + ".html" else it }

            val resp = app.get(
                fixedUrl,
                referer = referer,
                headers = mapOf(
                    "User-Agent"     to userAgent,
                    "Sec-Fetch-Dest" to "iframe"
                )
            ).text

            val m3u8Match = Regex("""file\s*:\s*['"](https?://[^'"]+\.m3u8[^'"]*)['"]""").find(resp)?.groupValues?.get(1)
                ?: Regex("""(https?://[^\s'"]+\.m3u8[^\s'"]*)""").find(resp)?.groupValues?.get(1)

            if (m3u8Match != null) {
                val origin = Regex("""^(https?://[^/]+)""").find(fixedUrl)?.groupValues?.get(1) ?: "https://vidmoly.biz"
                Regex("""file\s*:\s*['"]([^'"]+\.(?:vtt|srt)[^'"]*)['"]\s*,\s*label\s*:\s*['"]([^'"]+)['"]""").findAll(resp).forEach { m ->
                    val subUrl = if (m.groupValues[1].startsWith("http")) m.groupValues[1] else "$origin${m.groupValues[1]}"
                    subtitleCallback.invoke(newSubtitleFile(m.groupValues[2], subUrl))
                }
                callback.invoke(
                    newExtractorLink(
                        source = name,
                        name   = "VidMoly (1080p Full HD)",
                        url    = m3u8Match,
                        type   = ExtractorLinkType.M3U8
                    ) {
                        this.referer = "$origin/"
                        this.headers = mapOf(
                            "User-Agent" to userAgent,
                            "Referer"    to "$origin/",
                            "Origin"     to origin
                        )
                        this.quality = Qualities.P1080.value
                    }
                )
                return true
            }
        } catch (_: Throwable) {
        }
        return false
    }
}
