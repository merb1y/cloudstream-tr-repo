package com.keyiflerolsun

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class SelcukSports : MainAPI() {
    override var mainUrl = "https://www.selcuksportshd1846.xyz"
    override var name = "SelçukSports"
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
            name = "beIN Sports 1 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports1/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports1.png",
            category = "beIN Sports Kanalları"
        ),
        ChannelItem(
            name = "beIN Sports 2 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports2/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports2.png",
            category = "beIN Sports Kanalları"
        ),
        ChannelItem(
            name = "beIN Sports 3 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports3/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports3.png",
            category = "beIN Sports Kanalları"
        ),
        ChannelItem(
            name = "beIN Sports 4 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports4/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports4.png",
            category = "beIN Sports Kanalları"
        ),
        ChannelItem(
            name = "beIN Sports 5 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports5/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports5.png",
            category = "beIN Sports Kanalları"
        ),
        ChannelItem(
            name = "S Sport 1 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukssport/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/ssport.png",
            category = "Diğer Spor Kanalları"
        ),
        ChannelItem(
            name = "S Sport 2 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukssport2/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/ssport2.png",
            category = "Diğer Spor Kanalları"
        ),
        ChannelItem(
            name = "Tivibu Spor 1 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcuktivibuspor/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/tivibuspor.png",
            category = "Diğer Spor Kanalları"
        ),
        ChannelItem(
            name = "Smart Spor 1 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcuksmartspor/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/smartspor.png",
            category = "Diğer Spor Kanalları"
        ),
        ChannelItem(
            name = "Tabii Spor 1 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcuktabiispor/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/tabiispor.png",
            category = "Diğer Spor Kanalları"
        ),
        ChannelItem(
            name = "TRT Spor HD",
            url = "https://trt.daioncdn.net/trtspor/master.m3u8?app=web&platform=trtspor",
            poster = "https://i.ibb.co/7Xk7yWn/trtspor.png",
            category = "Açık Spor Kanalları",
            referer = ""
        ),
        ChannelItem(
            name = "A Spor HD",
            url = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/aspor/aspor.m3u8",
            poster = "https://i.ibb.co/vzR0j4x/aspor.png",
            category = "Açık Spor Kanalları",
            referer = ""
        )
    )

    override val mainPage = mainPageOf(
        "" to "SelçukSports Canlı Maç Kanalları"
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
        val title = ch?.name ?: "SelçukSports Canlı Yayın"
        val poster = ch?.poster ?: "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports1.png"

        return newLiveStreamLoadResponse(title, url, url) {
            this.posterUrl = poster
            this.plot = "SelçukSports HD Canlı Yayın"
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val ch = channels.firstOrNull { it.url == data }
        val title = ch?.name ?: "SelçukSports"

        callback.invoke(
            newExtractorLink(
                source = name,
                name = "$title - SelçukSports (HD)",
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
