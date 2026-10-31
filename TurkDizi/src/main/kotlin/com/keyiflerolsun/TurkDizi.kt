package com.keyiflerolsun

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.lagradost.cloudstream3.Episode
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.Score
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.fixUrl
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.newEpisode
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newTvSeriesLoadResponse
import com.lagradost.cloudstream3.newTvSeriesSearchResponse
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.newExtractorLink
import org.jsoup.nodes.Document
import java.net.URLEncoder

/**
 * dizipal12.com.tr tabanlı Türk dizisi kaynağı.
 *
 * Ana sayfa bölümleri site içeriğinden, oynatma ise dizipal -> mntplayer
 * zincirinden (HLS master.m3u8) alınır.
 */
class TurkDizi : MainAPI() {
    override var mainUrl              = "https://dizipal12.com.tr"
    override var name                 = "TurkDizi"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = true
    override val supportedTypes       = setOf(TvType.TvSeries)

    private val apiBase = "$mainUrl/api/content.php?type=dizi"

    override val mainPage = mainPageOf(
        "sifir-bir"  to "Sıfır Bir",
        "yerli"      to "Yerli Diziler",
        "api:views"  to "En Çok İzlenen",
        "api:newest" to "Son Eklenen",
        "api:imdb"   to "IMDb Puanı Yüksek",
    )

    // ------------------------------------------------------------------ //
    //  Ana sayfa
    // ------------------------------------------------------------------ //

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val key = request.data
        var hasNext = false

        val items: List<SearchResponse> = when {
            key == "sifir-bir" -> listOfNotNull(
                seriesCard("Sıfır Bir", "$mainUrl/dizi/sifir-bir-239", "https://image.tmdb.org/t/p/w500/ctqW76RtS9FOayjruXmRccAw7SU.jpg")
            )

            key == "yerli" -> yerliDiziler.mapNotNull {
                seriesCard(it.title, "$mainUrl/dizi/${it.slug}", it.poster)
            }

            key.startsWith("api:") -> {
                val (list, next) = fetchListing(key.removePrefix("api:"), page)
                hasNext = next
                list
            }

            else -> emptyList()
        }

        return newHomePageResponse(request.name, items, hasNext)
    }

    private suspend fun fetchListing(sort: String, page: Int): Pair<List<SearchResponse>, Boolean> {
        return try {
            val response = app.get("$apiBase&page=$page&genre=&year=&sort=$sort")
            if (!response.isSuccessful) return emptyList<SearchResponse>() to false

            val parsed = tryParseJson<ListingResponse>(response.text)
                ?: return emptyList<SearchResponse>() to false

            val items = parsed.items.mapNotNull { item ->
                if (item.url.isBlank() || item.title.isBlank()) return@mapNotNull null
                newTvSeriesSearchResponse(item.title, item.url, TvType.TvSeries) {
                    this.posterUrl = item.poster
                    this.year = item.year
                    if (item.imdb > 0.0) this.score = Score.from10(item.imdb)
                }
            }

            items to (page < parsed.pages)
        } catch (_: Throwable) {
            emptyList<SearchResponse>() to false
        }
    }

    // ------------------------------------------------------------------ //
    //  Arama
    // ------------------------------------------------------------------ //

    override suspend fun search(query: String): List<SearchResponse> {
        val encoded = URLEncoder.encode(query, "UTF-8").replace("+", "%20")
        return try {
            val response = app.get(
                "$mainUrl/search?q=$encoded",
                headers = mapOf(
                    "X-Requested-With" to "XMLHttpRequest",
                    "Accept"           to "application/json",
                    "Referer"          to "$mainUrl/",
                ),
            )
            if (!response.isSuccessful) return emptyList()

            val parsed = tryParseJson<SearchApiResponse>(response.text) ?: return emptyList()
            parsed.results
                .filter { it.type == "dizi" }
                .mapNotNull { item ->
                    if (item.url.isBlank() || item.title.isBlank()) return@mapNotNull null
                    newTvSeriesSearchResponse(item.title, item.url, TvType.TvSeries) {
                        this.posterUrl = item.poster
                        this.year = item.year
                        if (item.imdb > 0.0) this.score = Score.from10(item.imdb)
                    }
                }
                .distinctBy { it.url }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    // ------------------------------------------------------------------ //
    //  Dizi detayı + bölüm listesi
    // ------------------------------------------------------------------ //

    override suspend fun load(url: String): LoadResponse? {
        val seriesUrl = seriesUrlOf(url)
        return try {
            val document = app.get(seriesUrl).document

            val title = document.selectFirst("h1.series-title")?.text()?.removeSuffix(" izle")?.trim()
                ?.ifBlank { null }
                ?: document.selectFirst("meta[property=og:title]")?.attr("content")?.substringBefore(" izle")?.trim()
                ?: return null

            val poster  = document.selectFirst("meta[property=og:image]")?.attr("content")
            val plot    = document.selectFirst("p.series-summary")?.text()?.trim()
            val meta    = document.selectFirst(".series-meta-mobile")?.text().orEmpty()
            val year    = Regex("(19|20)\\d{2}").find(meta)?.value?.toIntOrNull()
            val tags    = document.select("a.series-badge").map { it.text().trim() }.filter { it.isNotEmpty() }
            val episodes = parseEpisodes(document, seriesUrl)

            newTvSeriesLoadResponse(title, seriesUrl, TvType.TvSeries, episodes) {
                this.posterUrl = poster
                this.plot      = plot
                this.year      = year
                if (tags.isNotEmpty()) this.tags = tags
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun parseEpisodes(document: Document, seriesUrl: String): List<Episode> {
        val slugPath = Regex("/dizi/([^/?#]+)").find(seriesUrl)?.let { "/dizi/${it.groupValues[1]}" }
            ?: return emptyList()

        val json = extractEpisodesJson(document) ?: return emptyList()
        val seasons = tryParseJson<Map<String, Map<String, EpisodeJson>>>(json) ?: return emptyList()

        val episodes = mutableListOf<Episode>()
        for ((seasonKey, episodeMap) in seasons) {
            val seasonNumber = seasonKey.toIntOrNull() ?: continue
            for ((indexKey, episode) in episodeMap) {
                val index = indexKey.toIntOrNull() ?: continue
                val episodeUrl = "$mainUrl$slugPath/$seasonNumber-sezon/$index-bolum"
                episodes.add(
                    newEpisode(episodeUrl) {
                        this.name        = episode.title?.trim()?.ifBlank { null } ?: "Bölüm $index"
                        this.season      = seasonNumber
                        this.episode     = index
                        this.posterUrl   = episode.still_url
                        this.description = episode.summary
                    }
                )
            }
        }

        return episodes.sortedWith(compareBy({ it.season ?: 0 }, { it.episode ?: 0 }))
    }

    /** Dizi sayfasındaki `episodesData = {...}` betiğini ayıklar. */
    private fun extractEpisodesJson(document: Document): String? {
        for (script in document.select("script")) {
            val data = script.data()
            val marker = data.indexOf("episodesData")
            if (marker < 0) continue

            val objectStart = data.indexOf('{', marker)
            val arrayStart  = data.indexOf('[', marker)
            val start = when {
                objectStart < 0 -> arrayStart
                arrayStart  < 0 -> objectStart
                else            -> minOf(objectStart, arrayStart)
            }
            if (start < 0) continue

            val openChar  = data[start]
            val closeChar = if (openChar == '{') '}' else ']'
            var depth     = 0
            var inString  = false
            var escaped   = false

            for (i in start until data.length) {
                val ch = data[i]
                if (inString) {
                    when {
                        escaped -> escaped = false
                        ch == '\\' -> escaped = true
                        ch == '"'  -> inString = false
                    }
                } else {
                    when (ch) {
                        '"'       -> inString = true
                        openChar  -> depth++
                        closeChar -> {
                            depth--
                            if (depth == 0) return data.substring(start, i + 1)
                        }
                    }
                }
            }
        }
        return null
    }

    // ------------------------------------------------------------------ //
    //  Oynatma
    // ------------------------------------------------------------------ //

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        val watchUrl  = data
        val document  = try { app.get(watchUrl).document } catch (_: Throwable) { return false }

        val iframe = document.selectFirst("iframe#montana-frame")?.attr("src")?.trim()?.ifBlank { null }
            ?: document.selectFirst("iframe[src*=mntplayer], iframe[src*=/series/]")?.attr("src")?.trim()?.ifBlank { null }
            ?: return false

        val playerUrl = try { fixUrl(iframe) } catch (_: Throwable) { iframe }

        val playerHtml = try {
            app.get(playerUrl, referer = watchUrl).text
        } catch (_: Throwable) {
            return false
        }

        val dataSrc    = Regex("data-src=\"([^\"]+)\"").find(playerHtml)?.groupValues?.get(1)
        val directM3u8 = Regex("https?://[^\\s\"'<>]+\\.m3u8[^\\s\"'<>]*").find(playerHtml)?.value

        val streamUrl = when {
            !dataSrc.isNullOrBlank() && dataSrc.startsWith("http") -> dataSrc
            !dataSrc.isNullOrBlank() -> {
                val origin = Regex("^https?://[^/]+").find(playerUrl)?.value ?: "https://vod.mntplayer.com:8443"
                origin + dataSrc
            }
            directM3u8 != null -> directM3u8
            else -> return false
        }

        callback.invoke(
            newExtractorLink(
                source = name,
                name   = "$name (HLS)",
                url    = streamUrl,
                type   = ExtractorLinkType.M3U8,
            ) {
                this.referer = watchUrl
                this.headers = mapOf("Referer" to watchUrl)
                this.quality = Qualities.Unknown.value
            }
        )
        return true
    }

    // ------------------------------------------------------------------ //
    //  Yardımcılar
    // ------------------------------------------------------------------ //

    private fun seriesUrlOf(url: String): String {
        val match = Regex("/dizi/([^/?#]+)").find(url) ?: return url
        return "$mainUrl/dizi/${match.groupValues[1]}"
    }

    private fun seriesCard(title: String, url: String, poster: String?): SearchResponse? {
        if (title.isBlank() || url.isBlank()) return null
        return newTvSeriesSearchResponse(title, url, TvType.TvSeries) {
            this.posterUrl = poster
        }
    }

    // ------------------------------------------------------------------ //
    //  Yerli dizi listesi
    // ------------------------------------------------------------------ //

    private data class YerliDizi(val title: String, val slug: String, val poster: String)

    private val yerliDiziler = listOf(
        YerliDizi("Sıfır Bir", "sifir-bir-239", "https://image.tmdb.org/t/p/w500/ctqW76RtS9FOayjruXmRccAw7SU.jpg"),
        YerliDizi("Çukur", "cukur", "https://image.tmdb.org/t/p/w500/xtCzLQOocIajjQ5Mhwp834BQhVX.jpg"),
        YerliDizi("Kurtlar Vadisi", "kurtlar-vadisi", "https://image.tmdb.org/t/p/w500/aoZdsIhGccKcLXu6f1g0sxiABws.jpg"),
        YerliDizi("Kurtlar Vadisi: Pusu", "kurtlar-vadisi-pusu", "https://image.tmdb.org/t/p/w500/60H7NVQ9W8fto57rD9RbNzR1clu.jpg"),
        YerliDizi("Eşkıya Dünyaya Hükümdar Olmaz", "eskiya-dunyaya-hukumdar-olmaz", "https://image.tmdb.org/t/p/w500/7JXIMbLcKW4tDqTYhl6V3giXo10.jpg"),
        YerliDizi("Diriliş: Ertuğrul", "dirilis-ertugrul-15", "https://image.tmdb.org/t/p/w500/xTwCPMFshsjtA0firQMe3GapQz9.jpg"),
        YerliDizi("Kuruluş: Osman", "kurulus-osman", "https://image.tmdb.org/t/p/w500/puLaqdcTRwFxwv9S3PO6jA3ep7J.jpg"),
        YerliDizi("Behzat Ç.", "behzat-c-bir-ankara-polisiyesi-25", "https://image.tmdb.org/t/p/w500/pF1OQe4zaXX9vOwxbxjZzws0CAe.jpg"),
        YerliDizi("Çekiç ve Gül", "cekic-ve-gul-bir-behzat-c-hikayesi-135", "https://image.tmdb.org/t/p/w500/w1WgrmhZJuQScjCaOAXBt5ziwZz.jpg"),
        YerliDizi("Leyla ile Mecnun", "leyla-ile-mecnun", "https://image.tmdb.org/t/p/w500/aEQEIXDmjd9otezUu64VFmpO51K.jpg"),
        YerliDizi("Muhteşem Yüzyıl", "muhtesem-yuzyil", "https://image.tmdb.org/t/p/w500/gn8k4ztPnrURIIocMOYdWCFknKJ.jpg"),
        YerliDizi("Gassal", "gassal-123", "https://image.tmdb.org/t/p/w500/ydZ4ZBlBOuL8NtVCNw5IthnRf4E.jpg"),
        YerliDizi("Yargı", "yargi", "https://image.tmdb.org/t/p/w500/fCwZOi3cTEOi2UkhjJNbFQnl4IW.jpg"),
        YerliDizi("Gibi", "gibi", "https://image.tmdb.org/t/p/w500/p7VgbdDN2LK41JTavzJ9x3i4ktE.jpg"),
        YerliDizi("Kardeş Payı", "kardes-payi", "https://image.tmdb.org/t/p/w500/uoFnDLca6zbVCPmvzJ6s5gGwVhA.jpg"),
        YerliDizi("Aşkla", "askla", "https://image.tmdb.org/t/p/w500/tlamrEuFPeXBjUNW4ynmJaAemV3.jpg"),
        YerliDizi("Yasak Elma", "yasak-elma", "https://image.tmdb.org/t/p/w500/5i6HPy9lzfVnqQYNNz9LyqyaID1.jpg"),
        YerliDizi("Sen Çal Kapımı", "sen-cal-kapimi", "https://image.tmdb.org/t/p/w500/9qq8LpOoBLYq8MxiMugn0gf9qJd.jpg"),
        YerliDizi("Erkenci Kuş", "erkenci-kus", "https://image.tmdb.org/t/p/w500/uJ3HalVLkaOeH1lvDdrEmeqFxds.jpg"),
        YerliDizi("Kiralık Aşk", "kiralik-ask", "https://image.tmdb.org/t/p/w500/7l7K63FeW6iUTJZPa6ips71OWDn.jpg"),
        YerliDizi("Yalı Çapkını", "yali-capkini", "https://image.tmdb.org/t/p/w500/vDqCoaMU5FUAuUs0EvL4OAUCxJk.jpg"),
        YerliDizi("Kızılcık Şerbeti", "kizilcik-serbeti", "https://image.tmdb.org/t/p/w500/a4FHdQfPGT1T2w4DYUEGgRMxSgZ.jpg"),
        YerliDizi("Sumud", "sumud-74", "https://image.tmdb.org/t/p/w500/caWugoeZwTQyDkelDxLVBSrSla8.jpg"),
        YerliDizi("Sakıncalı", "sakincali-3", "https://image.tmdb.org/t/p/w500/gsrslcCgNFrs1RPWAAcc3clOu3a.jpg"),
    )

    // ------------------------------------------------------------------ //
    //  JSON modelleri
    // ------------------------------------------------------------------ //

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ListingResponse(
        val total: Int = 0,
        val page: Int = 1,
        val pages: Int = 0,
        val items: List<ListingItem> = emptyList(),
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ListingItem(
        val url: String = "",
        val poster: String? = null,
        val title: String = "",
        val imdb: Double = 0.0,
        val year: Int? = null,
        val views: Long? = null,
        val genres: List<String> = emptyList(),
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class SearchApiResponse(
        val results: List<SearchItem> = emptyList(),
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class SearchItem(
        val title: String = "",
        val url: String = "",
        val poster: String? = null,
        val year: Int? = null,
        val imdb: Double = 0.0,
        val type: String? = null,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class EpisodeJson(
        val title: String? = null,
        val iframe_url: String? = null,
        val iframe_url_encrypted: String? = null,
        val still_url: String? = null,
        val summary: String? = null,
        val season: Int? = null,
    )
}
