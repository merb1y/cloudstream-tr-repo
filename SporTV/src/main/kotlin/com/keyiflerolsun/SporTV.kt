package com.keyiflerolsun

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class SporTV : MainAPI() {
    override var mainUrl = "https://spor.canlitv.local"
    override var name = "SporTV"
    override val hasMainPage = true
    override var lang = "tr"
    override val hasQuickSearch = true
    override val hasDownloadSupport = false
    override val supportedTypes = setOf(TvType.Live)

    data class ChannelItem(val name: String, val url: String, val poster: String, val category: String)

    private val sportsChannels = listOf(
        ChannelItem("A Spor HD", "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/aspor/aspor.m3u8", "https://i.ibb.co/vzR0j4x/aspor.png", "Spor Kanalları"),
        ChannelItem("TRT Spor HD", "https://trt.daioncdn.net/trtspor/master.m3u8?app=web&platform=trtspor", "https://i.ibb.co/7Xk7yWn/trtspor.png", "Spor Kanalları"),
        ChannelItem("TRT Spor Yıldız HD", "https://trt.daioncdn.net/trtspor-yildiz/master.m3u8?app=web&platform=trtspor", "https://i.ibb.co/Wc63v5c/trtspor-yildiz.png", "Spor Kanalları"),
        ChannelItem("HT Spor HD", "https://ciner.daioncdn.net/ht-spor/ht-spor.m3u8?app=web", "https://i.ibb.co/V3Kk7L6/htspor.png", "Spor Kanalları"),
        ChannelItem("TJK TV HD", "https://tjktv-live.tjk.org/tjktv.m3u8", "https://i.ibb.co/b3T62m2/tjktv.png", "At Yarışı & Jokey"),
        ChannelItem("TRT 1 HD (Canlı Maç & Avrupa)", "https://tv-trt1.medya.trt.com.tr/master.m3u8", "https://i.ibb.co/L5rK5gG/trt1.png", "Canlı Maç & Ulusal")
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val lists = sportsChannels.groupBy { it.category }.map { group ->
            val items = group.value.map { ch ->
                newLiveSearchResponse(ch.name, ch.url, TvType.Live) {
                    this.posterUrl = ch.poster
                }
            }
            HomePageList(group.key, items, isHorizontalImages = true)
        }
        return newHomePageResponse(lists, hasNext = false)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        return sportsChannels.filter { it.name.contains(query, ignoreCase = true) }.map { ch ->
            newLiveSearchResponse(ch.name, ch.url, TvType.Live) {
                this.posterUrl = ch.poster
            }
        }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse {
        val ch = sportsChannels.find { it.url == url } ?: sportsChannels.first()
        return newLiveStreamLoadResponse(ch.name, ch.url, url) {
            this.posterUrl = ch.poster
            this.plot = "${ch.name} - 7/24 Kesintisiz Canlı Yayın"
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val ch = sportsChannels.find { it.url == data }
        val title = ch?.name ?: "SporTV"
        callback.invoke(
            newExtractorLink(
                source = name,
                name = title,
                url = data,
                type = ExtractorLinkType.M3U8
            ) {
                this.quality = Qualities.P1080.value
            }
        )
        return true
    }
}
