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

    data class AlternativeLink(val name: String, val url: String, val referer: String = "")
    data class ChannelItem(
        val name: String,
        val url: String,
        val poster: String,
        val category: String,
        val referer: String = "",
        val alternatives: List<AlternativeLink> = emptyList()
    )

    private val sportsChannels = listOf(
        // 1. BEIN SPORTS & CANLI MAC KANALLARI
        ChannelItem(
            name = "beIN Sports 1 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports1/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports1.png",
            category = "beIN Sports (Canlı Maç)",
            referer = "https://www.selcuksportshd1846.xyz/",
            alternatives = listOf(
                AlternativeLink("Biraz Sports (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivebiraz1.m3u8", "https://birazcikspor44.xyz/"),
                AlternativeLink("Canlı TV Türk (1080p)", "https://andro.3749206.xyz/checklist/androstreamlivebs1.m3u8", "https://patronskor.cfd/")
            )
        ),
        ChannelItem(
            name = "beIN Sports 2 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports2/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports2.png",
            category = "beIN Sports (Canlı Maç)",
            referer = "https://www.selcuksportshd1846.xyz/",
            alternatives = listOf(
                AlternativeLink("Biraz Sports (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivebs2.m3u8", "https://birazcikspor44.xyz/")
            )
        ),
        ChannelItem(
            name = "beIN Sports 3 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports3/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports3.png",
            category = "beIN Sports (Canlı Maç)",
            referer = "https://www.selcuksportshd1846.xyz/",
            alternatives = listOf(
                AlternativeLink("Biraz Sports (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivebs3.m3u8", "https://birazcikspor44.xyz/")
            )
        ),
        ChannelItem(
            name = "beIN Sports 4 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports4/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports4.png",
            category = "beIN Sports (Canlı Maç)",
            referer = "https://www.selcuksportshd1846.xyz/",
            alternatives = listOf(
                AlternativeLink("Biraz Sports (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivebs4.m3u8", "https://birazcikspor44.xyz/")
            )
        ),
        ChannelItem(
            name = "beIN Sports 5 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports5/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/beinsports5.png",
            category = "beIN Sports (Canlı Maç)",
            referer = "https://www.selcuksportshd1846.xyz/",
            alternatives = listOf(
                AlternativeLink("Biraz Sports (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivebs5.m3u8", "https://birazcikspor44.xyz/")
            )
        ),

        // 2. DIGER OZEL SPOR KANALLARI (S Sport, Tivibu, Smart, Tabii)
        ChannelItem(
            name = "S Sport 1 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukssport/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/ssport.png",
            category = "Özel Spor Kanalları",
            referer = "https://www.selcuksportshd1846.xyz/"
        ),
        ChannelItem(
            name = "S Sport 2 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukssport2/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/ssport2.png",
            category = "Özel Spor Kanalları",
            referer = "https://www.selcuksportshd1846.xyz/"
        ),
        ChannelItem(
            name = "Tivibu Spor 1 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcuktivibuspor/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/tivibuspor.png",
            category = "Özel Spor Kanalları",
            referer = "https://www.selcuksportshd1846.xyz/"
        ),
        ChannelItem(
            name = "Smart Spor 1 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcuksmartspor/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/smartspor.png",
            category = "Özel Spor Kanalları",
            referer = "https://www.selcuksportshd1846.xyz/"
        ),
        ChannelItem(
            name = "Tabii Spor 1 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcuktabiispor/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/feroxx/test/main/Kanallar/Logolar/tabiispor.png",
            category = "Özel Spor Kanalları",
            referer = "https://www.selcuksportshd1846.xyz/"
        ),

        // 3. ULUSAL VE ACIK SPOR KANALLARI
        ChannelItem(
            name = "TRT 1 HD (Canlı Maç & UEFA)",
            url = "https://tv-trt1.medya.trt.com.tr/master.m3u8",
            poster = "https://i.ibb.co/L5rK5gG/trt1.png",
            category = "Ulusal Spor & Canlı Maç"
        ),
        ChannelItem(
            name = "A Spor HD",
            url = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/aspor/aspor.m3u8",
            poster = "https://i.ibb.co/vzR0j4x/aspor.png",
            category = "Ulusal Spor & Canlı Maç"
        ),
        ChannelItem(
            name = "TRT Spor HD",
            url = "https://trt.daioncdn.net/trtspor/master.m3u8?app=web&platform=trtspor",
            poster = "https://i.ibb.co/7Xk7yWn/trtspor.png",
            category = "Ulusal Spor & Canlı Maç"
        ),
        ChannelItem(
            name = "TRT Spor Yıldız HD",
            url = "https://trt.daioncdn.net/trtspor-yildiz/master.m3u8?app=web&platform=trtspor",
            poster = "https://i.ibb.co/Wc63v5c/trtspor-yildiz.png",
            category = "Ulusal Spor & Canlı Maç"
        ),
        ChannelItem(
            name = "HT Spor HD",
            url = "https://ciner.daioncdn.net/ht-spor/ht-spor.m3u8?app=web",
            poster = "https://i.ibb.co/V3Kk7L6/htspor.png",
            category = "Ulusal Spor & Canlı Maç"
        ),
        ChannelItem(
            name = "TJK TV HD",
            url = "https://tjktv-live.tjk.org/tjktv.m3u8",
            poster = "https://i.ibb.co/b3T62m2/tjktv.png",
            category = "Ulusal Spor & Canlı Maç"
        )
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
            this.plot = "${ch.name} - 7/24 Kesintisiz Canlı Maç & Spor Yayını"
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

        // 1. Ana Link
        callback.invoke(
            newExtractorLink(
                source = name,
                name = "$title (Ana Sunucu)",
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

        // 2. Yedek Alternatif Linkler
        ch?.alternatives?.forEach { alt ->
            callback.invoke(
                newExtractorLink(
                    source = name,
                    name = "$title - ${alt.name}",
                    url = alt.url,
                    type = ExtractorLinkType.M3U8
                ) {
                    if (alt.referer.isNotBlank()) {
                        this.headers = mapOf(
                            "Referer" to alt.referer,
                            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                        )
                    }
                    this.quality = Qualities.P1080.value
                }
            )
        }

        return true
    }
}
