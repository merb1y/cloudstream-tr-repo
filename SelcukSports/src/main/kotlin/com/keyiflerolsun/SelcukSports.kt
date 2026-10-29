package com.keyiflerolsun

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class SelcukSports : MainAPI() {
    override var mainUrl = "https://spor.canlitv.local"
    override var name = "SelçukSports"
    override val hasMainPage = true
    override var lang = "tr"
    override val hasQuickSearch = true
    override val hasDownloadSupport = false
    override val supportedTypes = setOf(TvType.Live)

    override val mainPage = mainPageOf(
        "" to "SelcukSports Canli Yayinlar"
    )

    private val ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36"

    data class AlternativeLink(val name: String, val url: String, val referer: String = "", val origin: String = "")
    data class ChannelItem(
        val name: String,
        val url: String,
        val poster: String,
        val category: String,
        val referer: String = "",
        val origin: String = "",
        val beyazSlug: String? = null,
        val atomId: String? = null,
        val interSlug: String? = null,
        val alternatives: List<AlternativeLink> = emptyList()
    )

    private val sportsChannels = listOf(
        // 1. BEIN SPORTS & CANLI MAC KANALLARI (8+ Calisan Sunucu)
        ChannelItem(
            name = "beIN Sports 1 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports1/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/bein1.png",
            category = "beIN Sports (Canlı Maç)",
            referer = "https://www.selcuksportshd1846.xyz/",
            origin = "https://www.selcuksportshd1846.xyz",
            beyazSlug = "tr-bein-sports-1-hd",
            atomId = "bein-sports-1",
            interSlug = "bein-sports-1",
            alternatives = listOf(
                AlternativeLink("NetSpor / BirazcıkSpor (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivebiraz1.m3u8", "https://birazcikspor44.xyz/", "https://birazcikspor44.xyz"),
                AlternativeLink("MahsunSports Sunucu-1 (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivebs1.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz"),
                AlternativeLink("MahsunSports Sunucu-2 (1080p)", "https://andro.evrenesoglu211.click/checklist/androstreamlivebs1.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz"),
                AlternativeLink("MahsunSports Sunucu-3 (1080p)", "https://andro.evrenesoglu215.click/checklist/androstreamlivebs1.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz"),
                AlternativeLink("Domates TV / PatronSkor (1080p)", "https://andro.3749206.xyz/checklist/androstreamlivebs1.m3u8", "https://patronskor.cfd/", "https://patronskor.cfd")
            )
        ),
        ChannelItem(
            name = "beIN Sports 2 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports2/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/bein2.png",
            category = "beIN Sports (Canlı Maç)",
            referer = "https://www.selcuksportshd1846.xyz/",
            origin = "https://www.selcuksportshd1846.xyz",
            beyazSlug = "tr-bein-sports-2-hd",
            atomId = "bein-sports-2",
            interSlug = "bein-sports-2",
            alternatives = listOf(
                AlternativeLink("NetSpor / MahsunSports-1 (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivebs2.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz"),
                AlternativeLink("MahsunSports Sunucu-2 (1080p)", "https://andro.evrenesoglu211.click/checklist/androstreamlivebs2.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz"),
                AlternativeLink("MahsunSports Sunucu-3 (1080p)", "https://andro.evrenesoglu215.click/checklist/androstreamlivebs2.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz"),
                AlternativeLink("Domates TV / PatronSkor (1080p)", "https://andro.3749206.xyz/checklist/androstreamlivebs2.m3u8", "https://patronskor.cfd/", "https://patronskor.cfd")
            )
        ),
        ChannelItem(
            name = "beIN Sports 3 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports3/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/bein3.png",
            category = "beIN Sports (Canlı Maç)",
            referer = "https://www.selcuksportshd1846.xyz/",
            origin = "https://www.selcuksportshd1846.xyz",
            beyazSlug = "tr-bein-sports-3-fhd",
            atomId = "bein-sports-3",
            interSlug = "bein-sports-3",
            alternatives = listOf(
                AlternativeLink("NetSpor / MahsunSports-1 (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivebs3.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz"),
                AlternativeLink("MahsunSports Sunucu-2 (1080p)", "https://andro.evrenesoglu211.click/checklist/androstreamlivebs3.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz"),
                AlternativeLink("Domates TV / PatronSkor (1080p)", "https://andro.3749206.xyz/checklist/androstreamlivebs3.m3u8", "https://patronskor.cfd/", "https://patronskor.cfd")
            )
        ),
        ChannelItem(
            name = "beIN Sports 4 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports4/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/bein4.png",
            category = "beIN Sports (Canlı Maç)",
            referer = "https://www.selcuksportshd1846.xyz/",
            origin = "https://www.selcuksportshd1846.xyz",
            beyazSlug = "tr-bein-sports-4",
            atomId = "bein-sports-4",
            interSlug = "bein-sports-4",
            alternatives = listOf(
                AlternativeLink("NetSpor / MahsunSports-1 (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivebs4.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz"),
                AlternativeLink("MahsunSports Sunucu-2 (1080p)", "https://andro.evrenesoglu211.click/checklist/androstreamlivebs4.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz"),
                AlternativeLink("Domates TV / PatronSkor (1080p)", "https://andro.3749206.xyz/checklist/androstreamlivebs4.m3u8", "https://patronskor.cfd/", "https://patronskor.cfd")
            )
        ),
        ChannelItem(
            name = "beIN Sports 5 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports5/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/bein5.png",
            category = "beIN Sports (Canlı Maç)",
            referer = "https://www.selcuksportshd1846.xyz/",
            origin = "https://www.selcuksportshd1846.xyz",
            beyazSlug = "beinsports-5",
            interSlug = "bein-sports-5",
            alternatives = listOf(
                AlternativeLink("NetSpor / MahsunSports-1 (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivebs5.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz"),
                AlternativeLink("MahsunSports Sunucu-2 (1080p)", "https://andro.evrenesoglu211.click/checklist/androstreamlivebs5.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz")
            )
        ),
        ChannelItem(
            name = "beIN Sports Max 1 HD",
            url = "https://andro.evrenesoglu219.click/checklist/androstreamlivebsm1.m3u8",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/bein1.png",
            category = "beIN Sports (Canlı Maç)",
            referer = "https://mahsunsports55.xyz/",
            origin = "https://mahsunsports55.xyz",
            beyazSlug = "tr-bein-sports-max-1-hd",
            interSlug = "bein-sports-max-1",
            alternatives = listOf(
                AlternativeLink("MahsunSports Sunucu-2 (1080p)", "https://andro.evrenesoglu211.click/checklist/androstreamlivebsm1.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz")
            )
        ),
        ChannelItem(
            name = "beIN Sports Max 2 HD",
            url = "https://andro.evrenesoglu219.click/checklist/androstreamlivebsm2.m3u8",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/bein2.png",
            category = "beIN Sports (Canlı Maç)",
            referer = "https://mahsunsports55.xyz/",
            origin = "https://mahsunsports55.xyz",
            beyazSlug = "tr-bein-sports-max-2-fhd",
            interSlug = "bein-sports-max-2",
            alternatives = listOf(
                AlternativeLink("MahsunSports Sunucu-2 (1080p)", "https://andro.evrenesoglu211.click/checklist/androstreamlivebsm2.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz")
            )
        ),

        // 2. DIGER OZEL SPOR KANALLARI (S Sport, Tivibu, Smart, Tabii)
        ChannelItem(
            name = "S Sport 1 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukssport/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/ssport1.png",
            category = "Özel Spor Kanalları",
            referer = "https://www.selcuksportshd1846.xyz/",
            origin = "https://www.selcuksportshd1846.xyz",
            beyazSlug = "tr-s-sport",
            atomId = "s-sport",
            interSlug = "s-sport",
            alternatives = listOf(
                AlternativeLink("MahsunSports (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivess1.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz")
            )
        ),
        ChannelItem(
            name = "S Sport 2 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukssport2/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/ssport2.png",
            category = "Özel Spor Kanalları",
            referer = "https://www.selcuksportshd1846.xyz/",
            origin = "https://www.selcuksportshd1846.xyz",
            beyazSlug = "tr-s-sport-2-hd",
            atomId = "s-sport-2",
            interSlug = "s-sport-2",
            alternatives = listOf(
                AlternativeLink("MahsunSports (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivess2.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz")
            )
        ),
        ChannelItem(
            name = "Tivibu Spor 1 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcuktivibuspor/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/tivibu1.png",
            category = "Özel Spor Kanalları",
            referer = "https://www.selcuksportshd1846.xyz/",
            origin = "https://www.selcuksportshd1846.xyz",
            beyazSlug = "tr-tivibu-spor-1-sd",
            interSlug = "tivibu-spor-1",
            alternatives = listOf(
                AlternativeLink("MahsunSports (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivets1.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz")
            )
        ),
        ChannelItem(
            name = "Smart Spor 1 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcuksmartspor/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/smart1.png",
            category = "Özel Spor Kanalları",
            referer = "https://www.selcuksportshd1846.xyz/",
            origin = "https://www.selcuksportshd1846.xyz",
            beyazSlug = "tr-smart-sport-hd-2",
            interSlug = "spor-smart",
            alternatives = listOf(
                AlternativeLink("MahsunSports (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivesm1.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz")
            )
        ),
        ChannelItem(
            name = "Tabii Spor 1 HD",
            url = "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcuktabiispor/playlist.m3u8",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/tabii.png",
            category = "Özel Spor Kanalları",
            referer = "https://www.selcuksportshd1846.xyz/",
            origin = "https://www.selcuksportshd1846.xyz",
            beyazSlug = "tr-tabii-spor-fhd",
            alternatives = listOf(
                AlternativeLink("MahsunSports (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivetb1.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz")
            )
        ),

        // 3. ULUSAL VE ACIK SPOR KANALLARI
        ChannelItem(
            name = "TRT 1 HD (Canlı Maç & UEFA)",
            url = "https://tv-trt1.medya.trt.com.tr/master.m3u8",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/trt1.png",
            category = "Ulusal Spor & Canlı Maç"
        ),
        ChannelItem(
            name = "A Spor HD",
            url = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/aspor/aspor.m3u8",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/aspor.png",
            category = "Ulusal Spor & Canlı Maç"
        ),
        ChannelItem(
            name = "TRT Spor HD",
            url = "https://trt.daioncdn.net/trtspor/master.m3u8?app=web&platform=trtspor",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/trtspor.png",
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
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/htspor.png",
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
            this.plot = "${ch.name} - 8 Ayrı Çalışan Sunuculu Canlı Maç & Spor Yayını"
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val ch = sportsChannels.find { it.url == data }
        val title = ch?.name ?: "SelçukSports"

        // 1. Ana Link (SelçukSports HD)
        callback.invoke(
            newExtractorLink(
                source = name,
                name = "$title - SelçukSports (1080p)",
                url = data,
                type = ExtractorLinkType.M3U8
            ) {
                if (!ch?.referer.isNullOrBlank()) {
                    this.referer = ch!!.referer
                    this.headers = buildMap {
                        put("Referer", ch.referer)
                        put("User-Agent", ua)
                        if (ch.origin.isNotBlank()) put("Origin", ch.origin)
                    }
                }
                this.quality = Qualities.P1080.value
            }
        )

        // 2. BeyazElma Edge-1 & Edge-2 Dinamik Sunucuları
        if (ch?.beyazSlug != null) {
            runCatching {
                val json = app.get(
                    "https://embed.beyazelma78.com/api/play/${ch.beyazSlug}",
                    headers = mapOf(
                        "User-Agent" to ua,
                        "Referer"    to "https://beyazelma78.com/",
                        "Origin"     to "https://beyazelma78.com"
                    )
                ).text
                val primary = Regex(""""primary"\s*:\s*"([^"]+)"""").find(json)?.groupValues?.get(1)
                val fallbacks = Regex(""""fallbacks"\s*:\s*\[(.*?)\]""").find(json)?.groupValues?.get(1)
                    ?.let { Regex(""""(https?://[^"]+)"""").findAll(it).map { m -> m.groupValues[1] }.toList() }
                    ?: emptyList()

                if (!primary.isNullOrBlank()) {
                    callback.invoke(
                        newExtractorLink(name, "$title - BeyazElma Edge-1 (1080p)", primary, ExtractorLinkType.M3U8) {
                            this.referer = "https://beyazelma78.com/"
                            this.headers = mapOf(
                                "User-Agent" to ua,
                                "Referer"    to "https://beyazelma78.com/",
                                "Origin"     to "https://beyazelma78.com"
                            )
                            this.quality = Qualities.P1080.value
                        }
                    )
                }
                fallbacks.forEachIndexed { idx, fb ->
                    callback.invoke(
                        newExtractorLink(name, "$title - BeyazElma Edge-${idx + 2} (1080p)", fb, ExtractorLinkType.M3U8) {
                            this.referer = "https://beyazelma78.com/"
                            this.headers = mapOf(
                                "User-Agent" to ua,
                                "Referer"    to "https://beyazelma78.com/",
                                "Origin"     to "https://beyazelma78.com"
                            )
                            this.quality = Qualities.P1080.value
                        }
                    )
                }
            }
        }

        // 3. İnterSporTV Cloudflare CDN Sunucusu
        if (ch?.interSlug != null) {
            callback.invoke(
                newExtractorLink(
                    name,
                    "$title - İnterSporTV (Cloudflare CDN)",
                    "https://pix.ruhhi.workers.dev/${ch.interSlug}.m3u8",
                    ExtractorLinkType.M3U8
                ) {
                    this.referer = "https://intersportv1.live/"
                    this.headers = mapOf(
                        "User-Agent" to ua,
                        "Referer"    to "https://intersportv1.live/",
                        "Origin"     to "https://intersportv1.live"
                    )
                    this.quality = Qualities.P1080.value
                }
            )
        }

        // 4. AtomSpor / TeleTV Dinamik Sunucusu
        if (ch?.atomId != null) {
            runCatching {
                val resp = app.get(
                    "https://teletv15.top/load/yayinlink.php?id=${ch.atomId}",
                    headers = mapOf(
                        "User-Agent" to ua,
                        "Referer"    to "https://atomsportv519.top/",
                        "Origin"     to "https://atomsportv519.top"
                    )
                ).text
                val stream = Regex(""""deismackanal"\s*:\s*"([^"]+\.m3u8[^"]*)"""").find(resp)?.groupValues?.get(1)
                    ?.replace("\\/", "/")
                if (!stream.isNullOrBlank()) {
                    callback.invoke(
                        newExtractorLink(name, "$title - AtomSpor / TeleTV (1080p)", stream, ExtractorLinkType.M3U8) {
                            this.referer = "https://atomsportv519.top/"
                            this.headers = mapOf(
                                "User-Agent" to ua,
                                "Referer"    to "https://atomsportv519.top/",
                                "Origin"     to "https://atomsportv519.top"
                            )
                            this.quality = Qualities.P1080.value
                        }
                    )
                }
            }
        }

        // 5. Yedek Alternatif Sunucular (NetSpor, MahsunSports 1-3, DomatesTV)
        ch?.alternatives?.forEach { alt ->
            callback.invoke(
                newExtractorLink(
                    source = name,
                    name = "$title - ${alt.name}",
                    url = alt.url,
                    type = ExtractorLinkType.M3U8
                ) {
                    if (alt.referer.isNotBlank()) {
                        this.referer = alt.referer
                        this.headers = buildMap {
                            put("Referer", alt.referer)
                            put("User-Agent", ua)
                            if (alt.origin.isNotBlank()) put("Origin", alt.origin)
                        }
                    }
                    this.quality = Qualities.P1080.value
                }
            )
        }

        return true
    }
}
