package com.keyiflerolsun

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class DiziYou : MainAPI() {
    override var mainUrl = "https://www.diziyou.one"
    override var name = "DiziYou"
    override val hasMainPage = true
    override var lang = "tr"
    override val hasQuickSearch = false
    override val supportedTypes = setOf(TvType.TvSeries)

    override val mainPage = mainPageOf(
        "${mainUrl}/" to "Popüler Diziler",
        "${mainUrl}/page/1/" to "Tüm Diziler"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        return try {
            val url = if (page == 1) {
                request.data
            } else {
                "${mainUrl}/page/$page/"
            }
            val document = app.get(url).document
            val items = document.select("div#list-series-main a, div.cat-container-main a, div.poster a, ul.alphabetical-category-list li a")
            val list = items.mapNotNull { it.toSearchResult() }.distinctBy { it.url }
            val hasNext = document.select("a.next, a[href*='/page/${page + 1}/']").isNotEmpty()
            newHomePageResponse(request.name, list, hasNext = hasNext)
        } catch (e: Throwable) {
            newHomePageResponse(request.name, emptyList(), hasNext = false)
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        return try {
            val document = app.get("${mainUrl}/?s=${query}").document
            val items = document.select("div#list-series-main a, div.cat-container-main a, div.poster a, div.search-results a, ul.alphabetical-category-list li a")
            items.mapNotNull { it.toSearchResult() }.distinctBy { it.url }
        } catch (e: Throwable) {
            emptyList()
        }
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val href = fixUrlNull(this.attr("href")) ?: return null
        if (!href.startsWith("http") || href.contains("/wp-") || href.contains("/page/")) return null
        val img = this.selectFirst("img")
        val title = this.attr("title").ifBlank {
            img?.attr("alt")?.ifBlank { this.text().trim() }
        } ?: this.text().trim()
        if (title.isBlank()) return null
        val poster = fixUrlNull(img?.attr("src") ?: img?.attr("data-src"))

        return newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
            this.posterUrl = poster
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val title = document.selectFirst("h1")?.text()?.trim()
            ?.removeSuffix(" izle")
            ?.removeSuffix(" - Türkçe Dublaj & Altyazılı | Diziyou")
            ?: document.title().split(" izle")[0].trim()
        val poster = fixUrlNull(document.selectFirst("div.poster img")?.attr("src")
            ?: document.selectFirst("div.series-poster img")?.attr("src")
            ?: document.selectFirst("meta[property='og:image']")?.attr("content"))

        val plot = document.selectFirst("div.series-summary, div.summary, div.story-text, p.description")?.text()?.trim()
        val year = Regex("""(\d{4})""").find(document.selectFirst("div.series-info, span.date")?.text() ?: "")?.groupValues?.get(1)?.toIntOrNull()

        val episodes = mutableListOf<Episode>()
        val epLinks = document.select("a[href*='-sezon-']")
        for (ep in epLinks) {
            val epHref = fixUrlNull(ep.attr("href")) ?: continue
            val epText = ep.text().trim()
            val match = Regex("""(\d+)-sezon-(\d+)-bolum""").find(epHref)
                ?: Regex("""(\d+)\.?\s*Sezon.*?(\d+)\.?\s*Bölüm""").find(epText)

            val sNum = match?.groupValues?.get(1)?.toIntOrNull() ?: 1
            val eNum = match?.groupValues?.get(2)?.toIntOrNull()

            episodes.add(newEpisode(epHref) {
                this.name = epText.ifBlank { "Bölüm $eNum" }
                this.season = sNum
                this.episode = eNum
            })
        }

        val sortedEpisodes = episodes.distinctBy { it.data }.sortedWith(compareBy({ it.season }, { it.episode }))

        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, sortedEpisodes) {
            this.posterUrl = poster
            this.plot = plot
            this.year = year
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = app.get(data).document
        val iframeSrc = doc.selectFirst("iframe")?.attr("src") ?: return false
        val fixedIframe = fixUrl(iframeSrc)

        val playerHtml = app.get(fixedIframe, referer = data).text
        val m3u8Match = Regex("""(https?://[^\s'"<>]+\.m3u8[^\s'"<>]*)""").find(playerHtml)?.groupValues?.get(1)

        if (m3u8Match != null) {
            callback.invoke(
                newExtractorLink(
                    source = name,
                    name = "DiziYou",
                    url = m3u8Match,
                    type = ExtractorLinkType.M3U8
                ) {
                    this.headers = mapOf("Referer" to fixedIframe)
                    this.quality = Qualities.P1080.value
                }
            )
            return true
        }

        // Try loadExtractor fallback
        loadExtractor(fixedIframe, data, subtitleCallback, callback)
        return true
    }
}
