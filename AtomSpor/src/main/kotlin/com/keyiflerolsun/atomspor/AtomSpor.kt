package com.keyiflerolsun.atomspor

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class AtomSpor : MainAPI() {
    override var mainUrl            = "https://atomsportv519.top"
    override var name               = "AtomSpor"
    override val hasMainPage        = true
    override var lang               = "tr"
    override val hasQuickSearch     = true
    override val hasDownloadSupport = false
    override val supportedTypes     = setOf(TvType.Live)

    private val ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36"

    data class StaticServer(val label: String, val url: String, val referer: String, val origin: String)

    data class ChannelDef(
        val id: String,
        val title: String,
        val category: String,
        val poster: String,
        val beyazSlug: String?,
        val atomId: String? = null,
        val interSlug: String? = null,
        val backups: List<StaticServer> = emptyList()
    )

    private val channels = listOf(
        ChannelDef(
            id = "bein1",
            title = "beIN Sports 1 FHD",
            category = "beIN Sports Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/bein1.png",
            beyazSlug = "tr-bein-sports-1-hd",
            atomId = "bein-sports-1",
            interSlug = "bein-sports-1",
            backups = listOf(
                StaticServer("SelçukSports (1080p)", "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports1/playlist.m3u8", "https://www.selcuksportshd1846.xyz/", "https://www.selcuksportshd1846.xyz"),
                StaticServer("MahsunSports Sunucu-1 (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivebs1.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz"),
                StaticServer("MahsunSports Sunucu-2 (1080p)", "https://andro.evrenesoglu211.click/checklist/androstreamlivebs1.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz"),
                StaticServer("DomatesTV / PatronSkor (1080p)", "https://andro.3749206.xyz/checklist/androstreamlivebs1.m3u8", "https://patronskor.cfd/", "https://patronskor.cfd")
            )
        ),
        ChannelDef(
            id = "bein2",
            title = "beIN Sports 2 FHD",
            category = "beIN Sports Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/bein2.png",
            beyazSlug = "tr-bein-sports-2-hd",
            atomId = "bein-sports-2",
            interSlug = "bein-sports-2",
            backups = listOf(
                StaticServer("SelçukSports (1080p)", "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports2/playlist.m3u8", "https://www.selcuksportshd1846.xyz/", "https://www.selcuksportshd1846.xyz"),
                StaticServer("MahsunSports Sunucu-1 (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivebs2.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz"),
                StaticServer("MahsunSports Sunucu-2 (1080p)", "https://andro.evrenesoglu211.click/checklist/androstreamlivebs2.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz"),
                StaticServer("DomatesTV / PatronSkor (1080p)", "https://andro.3749206.xyz/checklist/androstreamlivebs2.m3u8", "https://patronskor.cfd/", "https://patronskor.cfd")
            )
        ),
        ChannelDef(
            id = "bein3",
            title = "beIN Sports 3 FHD",
            category = "beIN Sports Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/bein3.png",
            beyazSlug = "tr-bein-sports-3-fhd",
            atomId = "bein-sports-3",
            interSlug = "bein-sports-3",
            backups = listOf(
                StaticServer("SelçukSports (1080p)", "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports3/playlist.m3u8", "https://www.selcuksportshd1846.xyz/", "https://www.selcuksportshd1846.xyz"),
                StaticServer("MahsunSports Sunucu-1 (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivebs3.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz"),
                StaticServer("DomatesTV / PatronSkor (1080p)", "https://andro.3749206.xyz/checklist/androstreamlivebs3.m3u8", "https://patronskor.cfd/", "https://patronskor.cfd")
            )
        ),
        ChannelDef(
            id = "bein4",
            title = "beIN Sports 4 FHD",
            category = "beIN Sports Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/bein4.png",
            beyazSlug = "tr-bein-sports-4",
            atomId = "bein-sports-4",
            interSlug = "bein-sports-4",
            backups = listOf(
                StaticServer("SelçukSports (1080p)", "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports4/playlist.m3u8", "https://www.selcuksportshd1846.xyz/", "https://www.selcuksportshd1846.xyz"),
                StaticServer("MahsunSports Sunucu-1 (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivebs4.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz"),
                StaticServer("DomatesTV / PatronSkor (1080p)", "https://andro.3749206.xyz/checklist/androstreamlivebs4.m3u8", "https://patronskor.cfd/", "https://patronskor.cfd")
            )
        ),
        ChannelDef(
            id = "bein5",
            title = "beIN Sports 5 FHD",
            category = "beIN Sports Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/bein5.png",
            beyazSlug = "beinsports-5",
            interSlug = "bein-sports-5",
            backups = listOf(
                StaticServer("SelçukSports (1080p)", "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukbeinsports5/playlist.m3u8", "https://www.selcuksportshd1846.xyz/", "https://www.selcuksportshd1846.xyz"),
                StaticServer("MahsunSports Sunucu-1 (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivebs5.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz")
            )
        ),
        ChannelDef(
            id = "beinmax1",
            title = "beIN Sports Max 1 HD",
            category = "beIN Sports Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/bein1.png",
            beyazSlug = "tr-bein-sports-max-1-hd",
            interSlug = "bein-sports-max-1",
            backups = listOf(
                StaticServer("MahsunSports (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivebsm1.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz")
            )
        ),
        ChannelDef(
            id = "beinmax2",
            title = "beIN Sports Max 2 HD",
            category = "beIN Sports Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/bein2.png",
            beyazSlug = "tr-bein-sports-max-2-fhd",
            interSlug = "bein-sports-max-2",
            backups = listOf(
                StaticServer("MahsunSports (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivebsm2.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz")
            )
        ),
        ChannelDef(
            id = "ssport1",
            title = "S Sport 1 HD",
            category = "Özel Spor Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/ssport1.png",
            beyazSlug = "tr-s-sport",
            atomId = "s-sport",
            interSlug = "s-sport",
            backups = listOf(
                StaticServer("SelçukSports (1080p)", "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukssport/playlist.m3u8", "https://www.selcuksportshd1846.xyz/", "https://www.selcuksportshd1846.xyz"),
                StaticServer("MahsunSports (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivess1.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz")
            )
        ),
        ChannelDef(
            id = "ssport2",
            title = "S Sport 2 HD",
            category = "Özel Spor Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/ssport2.png",
            beyazSlug = "tr-s-sport-2-hd",
            atomId = "s-sport-2",
            interSlug = "s-sport-2",
            backups = listOf(
                StaticServer("SelçukSports (1080p)", "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcukssport2/playlist.m3u8", "https://www.selcuksportshd1846.xyz/", "https://www.selcuksportshd1846.xyz"),
                StaticServer("MahsunSports (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivess2.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz")
            )
        ),
        ChannelDef(
            id = "tivibu1",
            title = "Tivibu Spor 1 HD",
            category = "Özel Spor Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/tivibu1.png",
            beyazSlug = "tr-tivibu-spor-1-sd",
            interSlug = "tivibu-spor-1",
            backups = listOf(
                StaticServer("SelçukSports (1080p)", "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcuktivibuspor/playlist.m3u8", "https://www.selcuksportshd1846.xyz/", "https://www.selcuksportshd1846.xyz"),
                StaticServer("MahsunSports (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivets1.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz")
            )
        ),
        ChannelDef(
            id = "tivibu2",
            title = "Tivibu Spor 2 HD",
            category = "Özel Spor Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/tivibu1.png",
            beyazSlug = "tr-tivibu-spor-2-sd",
            interSlug = "tivibu-spor-2",
            backups = listOf(
                StaticServer("MahsunSports (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivets2.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz")
            )
        ),
        ChannelDef(
            id = "tivibu3",
            title = "Tivibu Spor 3 HD",
            category = "Özel Spor Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/tivibu1.png",
            beyazSlug = "tr-tivibu-spor-3-hd",
            interSlug = "tivibu-spor-3",
            backups = listOf(
                StaticServer("MahsunSports (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivets3.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz")
            )
        ),
        ChannelDef(
            id = "smart1",
            title = "Smart Spor 1 HD",
            category = "Özel Spor Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/smart1.png",
            beyazSlug = "tr-smart-sport-hd-2",
            interSlug = "spor-smart",
            backups = listOf(
                StaticServer("SelçukSports (1080p)", "https://dga1op10s1u3leo.33a6243d11525c.click/live/selcuksmartspor/playlist.m3u8", "https://www.selcuksportshd1846.xyz/", "https://www.selcuksportshd1846.xyz"),
                StaticServer("MahsunSports (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlivesm1.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz")
            )
        )
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val lists = channels.groupBy { it.category }.map { (cat, list) ->
            HomePageList(
                cat,
                list.map { ch ->
                    newLiveSearchResponse(ch.title, "$mainUrl/kanal/${ch.id}", TvType.Live) {
                        this.posterUrl = ch.poster
                    }
                },
                isHorizontalImages = true
            )
        }
        return newHomePageResponse(lists, hasNext = false)
    }

    override suspend fun search(query: String): List<SearchResponse> =
        channels.filter { it.title.contains(query, ignoreCase = true) }.map { ch ->
            newLiveSearchResponse(ch.title, "$mainUrl/kanal/${ch.id}", TvType.Live) {
                this.posterUrl = ch.poster
            }
        }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse {
        val id = url.substringAfterLast('/')
        val ch = channels.find { it.id == id } ?: channels.first()
        return newLiveStreamLoadResponse(ch.title, "$mainUrl/kanal/${ch.id}", "$mainUrl/kanal/${ch.id}") {
            this.posterUrl = ch.poster
            this.plot = "${ch.title} - BeyazElma Çift Edge Sunuculu Canlı Yayın"
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val id = data.substringAfterLast('/')
        val ch = channels.find { it.id == id } ?: channels.first()

        // 1. BeyazElma Edge-1 & Edge-2 Dinamik Tokenli Sunucular
        if (ch.beyazSlug != null) {
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
                        newExtractorLink(name, "${ch.title} - BeyazElma Edge-1 (1080p)", primary, ExtractorLinkType.M3U8) {
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
                        newExtractorLink(name, "${ch.title} - BeyazElma Edge-${idx + 2} (1080p)", fb, ExtractorLinkType.M3U8) {
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

        // 2. İnterSporTV Cloudflare Worker Sunucusu
        if (ch.interSlug != null) {
            callback.invoke(
                newExtractorLink(
                    name,
                    "${ch.title} - İnterSporTV (Cloudflare CDN)",
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

        // 3. AtomSpor / TeleTV Dinamik Sunucusu
        if (ch.atomId != null) {
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
                        newExtractorLink(name, "${ch.title} - AtomSpor / TeleTV (1080p)", stream, ExtractorLinkType.M3U8) {
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

        // 4. Statik Yedek Sunucular (SelçukSports, MahsunSports 1-2, DomatesTV)
        ch.backups.forEach { srv ->
            callback.invoke(
                newExtractorLink(name, "${ch.title} - ${srv.label}", srv.url, ExtractorLinkType.M3U8) {
                    this.referer = srv.referer
                    this.headers = mapOf(
                        "User-Agent" to ua,
                        "Referer"    to srv.referer,
                        "Origin"     to srv.origin
                    )
                    this.quality = Qualities.P1080.value
                }
            )
        }

        return true
    }
}
