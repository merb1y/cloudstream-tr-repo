package com.keyiflerolsun

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class NetSpor : MainAPI() {
    override var mainUrl = "https://netspor.canlitv.local"
    override var name = "NetSpor"
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
        val referer: String = "https://birazcikspor44.xyz/"
    )

    private val channels = listOf(
        ChannelItem(
            name = "NetSpor - beIN Sports 1 HD",
            url = "https://andro.evrenesoglu219.click/checklist/androstreamlivebiraz1.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports1.png",
            category = "beIN Sports (Mahsun & NetSpor)"
        ),
        ChannelItem(
            name = "NetSpor - beIN Sports 2 HD",
            url = "https://andro.evrenesoglu219.click/checklist/androstreamlivebs2.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports2.png",
            category = "beIN Sports (Mahsun & NetSpor)"
        ),
        ChannelItem(
            name = "NetSpor - beIN Sports 3 HD",
            url = "https://andro.evrenesoglu219.click/checklist/androstreamlivebs3.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports3.png",
            category = "beIN Sports (Mahsun & NetSpor)"
        ),
        ChannelItem(
            name = "NetSpor - beIN Sports 4 HD",
            url = "https://andro.evrenesoglu219.click/checklist/androstreamlivebs4.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports4.png",
            category = "beIN Sports (Mahsun & NetSpor)"
        ),
        ChannelItem(
            name = "NetSpor - beIN Sports 5 HD",
            url = "https://andro.evrenesoglu219.click/checklist/androstreamlivebs5.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports5.png",
            category = "beIN Sports (Mahsun & NetSpor)"
        ),
        ChannelItem(
            name = "NetSpor - S Sport 1 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukssport/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/ssport.png",
            category = "Diğer Spor Kanalları",
            referer = "https://www.selcuksportshd1846.xyz/"
        ),
        ChannelItem(
            name = "NetSpor - S Sport 2 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukssport2/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/ssport2.png",
            category = "Diğer Spor Kanalları",
            referer = "https://www.selcuksportshd1846.xyz/"
        )
    )

    override val mainPage = mainPageOf(
        "" to "NetSpor & MahsunSports Kanalları"
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
        val title = ch?.name ?: "NetSpor Canlı Yayın"
        val poster = ch?.poster ?: "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports1.png"

        return newLiveStreamLoadResponse(title, url, url) {
            this.posterUrl = poster
            this.plot = "NetSpor 1080p Kesintisiz Canlı Yayın"
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val ch = channels.firstOrNull { it.url == data }
        val title = ch?.name ?: "NetSpor"

        callback.invoke(
            newExtractorLink(
                source = name,
                name = "$title (1080p)",
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
