package com.keyiflerolsun

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class DomatesTV : MainAPI() {
    override var mainUrl = "https://domatestv.canlitv.local"
    override var name = "DomatesTV"
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
        val referer: String = "https://patronskor.cfd/"
    )

    private val channels = listOf(
        ChannelItem(
            name = "Domates TV - beIN Sports 1 HD (ArdaSpor)",
            url = "https://andro.3749206.xyz/checklist/androstreamlivebs1.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports1.png",
            category = "beIN Sports Canlı Maç"
        ),
        ChannelItem(
            name = "Domates TV - beIN Sports 2 HD (ArdaSpor)",
            url = "https://andro.3749206.xyz/checklist/androstreamlivebs2.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports2.png",
            category = "beIN Sports Canlı Maç"
        ),
        ChannelItem(
            name = "Domates TV - beIN Sports 3 HD (ArdaSpor)",
            url = "https://andro.3749206.xyz/checklist/androstreamlivebs3.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports3.png",
            category = "beIN Sports Canlı Maç"
        ),
        ChannelItem(
            name = "Domates TV - beIN Sports 4 HD (ArdaSpor)",
            url = "https://andro.3749206.xyz/checklist/androstreamlivebs4.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports4.png",
            category = "beIN Sports Canlı Maç"
        ),
        ChannelItem(
            name = "Domates TV - A Spor HD",
            url = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/aspor/aspor.m3u8",
            poster = "https://i.ibb.co/vzR0j4x/aspor.png",
            category = "Açık Spor Kanalları",
            referer = ""
        ),
        ChannelItem(
            name = "Domates TV - TRT Spor HD",
            url = "https://trt.daioncdn.net/trtspor/master.m3u8?app=web&platform=trtspor",
            poster = "https://i.ibb.co/7Xk7yWn/trtspor.png",
            category = "Açık Spor Kanalları",
            referer = ""
        )
    )

    override val mainPage = mainPageOf(
        "" to "Domates TV & ArdaSpor Kanalları"
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
        val title = ch?.name ?: "Domates TV Canlı Yayın"
        val poster = ch?.poster ?: "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports1.png"

        return newLiveStreamLoadResponse(title, url, url) {
            this.posterUrl = poster
            this.plot = "Domates TV & ArdaSpor HD Canlı Maç Yayını"
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val ch = channels.firstOrNull { it.url == data }
        val title = ch?.name ?: "Domates TV"

        callback.invoke(
            newExtractorLink(
                source = name,
                name = "$title (Hızlı)",
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
