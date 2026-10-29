package com.keyiflerolsun

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class Taraftarium24 : MainAPI() {
    override var mainUrl = "https://taraftarium24.canlitv.local"
    override var name = "Taraftarium24"
    override val hasMainPage = true
    override var lang = "tr"
    override val hasQuickSearch = true
    override val hasDownloadSupport = false
    override val supportedTypes = setOf(TvType.Live)

    data class ChannelItem(
        val name: String,
        val url: String,
        val poster: String,
        val category: String,
        val referer: String = "https://www.selcuksportshd1846.xyz/"
    )

    private val channels = listOf(
        ChannelItem(
            name = "Taraftarium - beIN Sports 1 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports1/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports1.png",
            category = "beIN Sports Canlı"
        ),
        ChannelItem(
            name = "Taraftarium - beIN Sports 2 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports2/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports2.png",
            category = "beIN Sports Canlı"
        ),
        ChannelItem(
            name = "Taraftarium - beIN Sports 3 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports3/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports3.png",
            category = "beIN Sports Canlı"
        ),
        ChannelItem(
            name = "Taraftarium - beIN Sports 4 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports4/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports4.png",
            category = "beIN Sports Canlı"
        ),
        ChannelItem(
            name = "Taraftarium - S Sport 1 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukssport/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/ssport.png",
            category = "Özel Spor Kanalları"
        ),
        ChannelItem(
            name = "Taraftarium - Tivibu Spor HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcuktivibuspor/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/tivibuspor.png",
            category = "Özel Spor Kanalları"
        ),
        ChannelItem(
            name = "Taraftarium - Smart Spor HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcuksmartspor/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/smartspor.png",
            category = "Özel Spor Kanalları"
        ),
        ChannelItem(
            name = "Taraftarium - TRT 1 Canlı Maç",
            url = "https://tv-trt1.medya.trt.com.tr/master.m3u8",
            poster = "https://i.ibb.co/L5rK5gG/trt1.png",
            category = "Ulusal Spor Kanalları",
            referer = ""
        ),
        ChannelItem(
            name = "Taraftarium - A Spor HD",
            url = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/aspor/aspor.m3u8",
            poster = "https://i.ibb.co/vzR0j4x/aspor.png",
            category = "Ulusal Spor Kanalları",
            referer = ""
        )
    )

    override val mainPage = mainPageOf(
        "" to "Taraftarium24 Canlı Maç Kanalları"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val grouped = channels.groupBy { it.category }
        val homePages = grouped.map { (cat, list) ->
            val searchResults = list.map { ch ->
                newLiveSearchResponse(ch.name, ch.url, TvType.Live) {
                    this.posterUrl = ch.poster
                }
            }
            HomePageList(cat, searchResults)
        }
        return newHomePageResponse(homePages, hasNext = false)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val q = query.trim().lowercase()
        return channels
            .filter { it.name.lowercase().contains(q) || it.category.lowercase().contains(q) }
            .map { ch ->
                newLiveSearchResponse(ch.name, ch.url, TvType.Live) {
                    this.posterUrl = ch.poster
                }
            }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse {
        val ch = channels.firstOrNull { it.url == url }
        val title = ch?.name ?: "Taraftarium24 Canlı Yayın"
        val poster = ch?.poster ?: "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports1.png"

        return newLiveStreamLoadResponse(title, url, url) {
            this.posterUrl = poster
            this.plot = "Taraftarium24 HD Canlı Maç Yayını"
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val ch = channels.firstOrNull { it.url == data }
        val title = ch?.name ?: "Taraftarium24"

        callback.invoke(
            newExtractorLink(
                source = name,
                name = "$title - Canlı HLS",
                url = data,
                type = ExtractorLinkType.M3U8
            ) {
                if (!ch?.referer.isNullOrBlank()) {
                    this.headers = mapOf(
                        "Referer" to ch!!.referer,
                        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                    )
                }
                this.quality = Qualities.P1080.value
            }
        )

        return true
    }
}
