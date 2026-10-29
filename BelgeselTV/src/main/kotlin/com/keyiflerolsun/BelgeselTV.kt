package com.keyiflerolsun

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class BelgeselTV : MainAPI() {
    override var mainUrl = "https://belgesel.canlitv.local"
    override var name = "BelgeselTV"
    override val hasMainPage = true
    override var lang = "tr"
    override val hasQuickSearch = true
    override val hasDownloadSupport = false
    override val supportedTypes = setOf(TvType.Live)

    data class ChannelItem(val name: String, val url: String, val poster: String, val category: String)

    private val docChannels = listOf(
        ChannelItem("TRT Belgesel HD", "https://tv-trtbelgesel.medya.trt.com.tr/master.m3u8", "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/trtbelgesel.png", "Belgesel & Kültür Kanalları"),
        ChannelItem("TRT Müzik HD", "https://tv-trtmuzik.medya.trt.com.tr/master.m3u8", "https://i.ibb.co/3k5fH7Y/trtmuzik.png", "Belgesel & Kültür Kanalları"),
        ChannelItem("TRT Avaz HD", "https://tv-trtavaz.medya.trt.com.tr/master_720.m3u8", "https://i.ibb.co/r2p1Wvj/trtavaz.png", "Belgesel & Kültür Kanalları"),
        ChannelItem("TBMM TV (Meclis TV) HD", "https://meclistv-live.ercdn.net/meclistv/meclistv_720p.m3u8", "https://i.ibb.co/b3gY7cQ/tbmm.png", "Belgesel & Kültür Kanalları")
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val lists = docChannels.groupBy { it.category }.map { group ->
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
        return docChannels.filter { it.name.contains(query, ignoreCase = true) }.map { ch ->
            newLiveSearchResponse(ch.name, ch.url, TvType.Live) {
                this.posterUrl = ch.poster
            }
        }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse {
        val ch = docChannels.find { it.url == url } ?: docChannels.first()
        return newLiveStreamLoadResponse(ch.name, ch.url, url) {
            this.posterUrl = ch.poster
            this.plot = "${ch.name} - Kesintisiz Canlı Yayın"
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val ch = docChannels.find { it.url == data }
        val title = ch?.name ?: "BelgeselTV"
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
