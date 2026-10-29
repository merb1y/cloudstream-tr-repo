package com.keyiflerolsun

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class HaberTV : MainAPI() {
    override var mainUrl = "https://haber.canlitv.local"
    override var name = "HaberTV"
    override val hasMainPage = true
    override var lang = "tr"
    override val hasQuickSearch = true
    override val hasDownloadSupport = false
    override val supportedTypes = setOf(TvType.Live)

    data class ChannelItem(val name: String, val url: String, val poster: String, val category: String)

    private val newsChannels = listOf(
        ChannelItem("HaberTürk TV HD", "https://rmtftbjlne.turknet.ercdn.net/bpeytmnqyp/haberturktv/haberturktv_1080p.m3u8", "https://i.ibb.co/6Pq7p8Q/haberturk.png", "Haber Kanalları"),
        ChannelItem("Haber Global HD", "https://tv.ensonhaber.com/haberglobal/haberglobal.m3u8", "https://i.ibb.co/3WqQ33N/haberglobal.png", "Haber Kanalları"),
        ChannelItem("TV 24 HD", "https://turkmedya-live.ercdn.net/tv24/tv24_720p.m3u8", "https://i.ibb.co/hK70Hk8/tv24.png", "Haber Kanalları"),
        ChannelItem("TRT Haber HD", "https://tv-trthaber.medya.trt.com.tr/master_720.m3u8", "https://i.ibb.co/4T7VjPz/trthaber.png", "Haber Kanalları")
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val lists = newsChannels.groupBy { it.category }.map { group ->
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
        return newsChannels.filter { it.name.contains(query, ignoreCase = true) }.map { ch ->
            newLiveSearchResponse(ch.name, ch.url, TvType.Live) {
                this.posterUrl = ch.poster
            }
        }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse {
        val ch = newsChannels.find { it.url == url } ?: newsChannels.first()
        return newLiveStreamLoadResponse(ch.name, ch.url, url) {
            this.posterUrl = ch.poster
            this.plot = "${ch.name} - Canlı Haber Yayını"
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val ch = newsChannels.find { it.url == data }
        val title = ch?.name ?: "HaberTV"
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
