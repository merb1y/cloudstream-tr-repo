package com.keyiflerolsun.mahsunsports

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class MahsunSports : MainAPI() {
    override var mainUrl            = "https://mahsunsports55.xyz"
    override var name               = "MahsunSports"
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
        val beyazSlug: String? = null,
        val atomId: String? = null,
        val interSlug: String? = null,
        val backups: List<StaticServer> = emptyList()
    )

    private fun mahsun(stream: String, server: String = "Sunucu-1"): List<StaticServer> = listOf(
        StaticServer("MahsunSports $server (1080p)", "https://andro.evrenesoglu219.click/checklist/androstreamlive$stream.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz"),
        StaticServer("MahsunSports Sunucu-2 (1080p)", "https://andro.evrenesoglu211.click/checklist/androstreamlive$stream.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz"),
        StaticServer("MahsunSports Sunucu-3 (1080p)", "https://andro.evrenesoglu215.click/checklist/androstreamlive$stream.m3u8", "https://mahsunsports55.xyz/", "https://mahsunsports55.xyz")
    )

    private fun patron(stream: String): StaticServer = StaticServer(
        "DomatesTV / PatronSkor (1080p)",
        "https://andro.3749206.xyz/checklist/androstreamlive$stream.m3u8",
        "https://patronskor.cfd/",
        "https://patronskor.cfd"
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
            backups = mahsun("bs1") + patron("bs1")
        ),
        ChannelDef(
            id = "bein2",
            title = "beIN Sports 2 FHD",
            category = "beIN Sports Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/bein2.png",
            beyazSlug = "tr-bein-sports-2-hd",
            atomId = "bein-sports-2",
            interSlug = "bein-sports-2",
            backups = mahsun("bs2") + patron("bs2")
        ),
        ChannelDef(
            id = "bein3",
            title = "beIN Sports 3 FHD",
            category = "beIN Sports Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/bein3.png",
            beyazSlug = "tr-bein-sports-3-fhd",
            atomId = "bein-sports-3",
            interSlug = "bein-sports-3",
            backups = mahsun("bs3") + patron("bs3")
        ),
        ChannelDef(
            id = "bein4",
            title = "beIN Sports 4 FHD",
            category = "beIN Sports Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/bein4.png",
            beyazSlug = "tr-bein-sports-4",
            atomId = "bein-sports-4",
            interSlug = "bein-sports-4",
            backups = mahsun("bs4") + patron("bs4")
        ),
        ChannelDef(
            id = "bein5",
            title = "beIN Sports 5 FHD",
            category = "beIN Sports Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/bein5.png",
            beyazSlug = "beinsports-5",
            interSlug = "bein-sports-5",
            backups = mahsun("bs5") + patron("bs5")
        ),
        ChannelDef(
            id = "beinmax1",
            title = "beIN Sports Max 1 HD",
            category = "beIN Sports Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/bein1.png",
            beyazSlug = "tr-bein-sports-max-1-hd",
            interSlug = "bein-sports-max-1",
            backups = mahsun("bsm1")
        ),
        ChannelDef(
            id = "beinmax2",
            title = "beIN Sports Max 2 HD",
            category = "beIN Sports Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/bein2.png",
            beyazSlug = "tr-bein-sports-max-2-fhd",
            interSlug = "bein-sports-max-2",
            backups = mahsun("bsm2")
        ),
        ChannelDef(
            id = "ssport1",
            title = "S Sport 1 HD",
            category = "Özel Spor Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/ssport1.png",
            beyazSlug = "tr-s-sport",
            atomId = "s-sport",
            interSlug = "s-sport",
            backups = mahsun("ss1") + patron("ss1")
        ),
        ChannelDef(
            id = "ssport2",
            title = "S Sport 2 HD",
            category = "Özel Spor Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/ssport2.png",
            beyazSlug = "tr-s-sport-2-hd",
            atomId = "s-sport-2",
            interSlug = "s-sport-2",
            backups = mahsun("ss2") + patron("ss2")
        ),
        ChannelDef(
            id = "tivibu1",
            title = "Tivibu Spor 1 HD",
            category = "Özel Spor Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/tivibu1.png",
            beyazSlug = "tr-tivibu-spor-1-sd",
            interSlug = "tivibu-spor-1",
            backups = mahsun("ts1") + patron("ts1")
        ),
        ChannelDef(
            id = "tivibu2",
            title = "Tivibu Spor 2 HD",
            category = "Özel Spor Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/tivibu1.png",
            beyazSlug = "tr-tivibu-spor-2-sd",
            interSlug = "tivibu-spor-2",
            backups = mahsun("ts2")
        ),
        ChannelDef(
            id = "tivibu3",
            title = "Tivibu Spor 3 HD",
            category = "Özel Spor Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/tivibu1.png",
            beyazSlug = "tr-tivibu-spor-3-hd",
            interSlug = "tivibu-spor-3",
            backups = mahsun("ts3")
        ),
        ChannelDef(
            id = "smart1",
            title = "Smart Spor 1 HD",
            category = "Özel Spor Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/smart1.png",
            beyazSlug = "tr-smart-sport-hd-2",
            interSlug = "spor-smart",
            backups = mahsun("sm1") + patron("sm1")
        ),
        ChannelDef(
            id = "tabii1",
            title = "Tabii Spor 1 HD",
            category = "Özel Spor Kanalları",
            poster = "https://raw.githubusercontent.com/merb1y/cloudstream-tr-repo/main/assets/logos/tabii.png",
            beyazSlug = "tr-tabii-spor-fhd",
            backups = mahsun("tb1") + patron("tb1")
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
            this.plot = "${ch.title} - MahsunSports çoklu sunucu altyapılı canlı yayın"
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

        // 1. Statik Yedek Sunucular (MahsunSports 1-3 + Domates/PatronSkor)
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

        // 3. Dinamik Sunucuları (BeyazElma & AtomSpor) paralel ve hızlı timeout ile çöz
        listOf("beyaz", "atom").amap { src ->
            if (src == "beyaz" && ch.beyazSlug != null) {
                runCatching {
                    val json = app.get(
                        "https://embed.beyazelma78.com/api/play/${ch.beyazSlug}",
                        headers = mapOf(
                            "User-Agent" to ua,
                            "Referer"    to "https://beyazelma78.com/",
                            "Origin"     to "https://beyazelma78.com"
                        ),
                        timeout = 4
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
            } else if (src == "atom" && ch.atomId != null) {
                runCatching {
                    val resp = app.get(
                        "https://teletv15.top/load/yayinlink.php?id=${ch.atomId}",
                        headers = mapOf(
                            "User-Agent" to ua,
                            "Referer"    to "https://atomsportv519.top/",
                            "Origin"     to "https://atomsportv519.top"
                        ),
                        timeout = 4
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
        }

        return true
    }
}
