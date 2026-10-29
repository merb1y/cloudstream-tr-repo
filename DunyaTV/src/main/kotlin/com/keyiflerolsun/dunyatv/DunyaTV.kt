package com.keyiflerolsun.dunyatv

import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.AppUtils.parseJson
import com.lagradost.cloudstream3.utils.AppUtils.toJson
import java.io.InputStream

/**
 * DunyaTV - iptv-org "Turkiye" listesini okuyan canli TV saglayicisi.
 * Liste: https://iptv-org.github.io/iptv/languages/tur.m3u
 */
class DunyaTV : MainAPI() {
    override var mainUrl            = "https://iptv-org.github.io/iptv/languages/tur.m3u"
    override var name               = "DunyaTV"
    override val hasMainPage        = true
    override var lang               = "tr"
    override val hasQuickSearch     = true
    override val hasDownloadSupport = false
    override val supportedTypes     = setOf(TvType.Live)

    private suspend fun kanallar(): List<PlaylistItem> =
        IptvPlaylistParser().parseM3U(app.get(mainUrl).text).items
            .filter { !it.url.isNullOrBlank() }

    /** "General;News" gibi Ingilizce grup adlarini Turkceye cevirir. */
    private fun grupAdi(ham: String?): String {
        val g = ham?.trim().orEmpty()
        if (g.isBlank()) return "Diğer"
        return g.split(";")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { GRUP_CEVIRI[it] ?: it }
            .distinct()
            .joinToString(", ")
            .ifBlank { "Diğer" }
    }

    /** Kanal adindaki "(720p)", "[Geo-blocked]", "[Not 24/7]" gibi etiketleri temizler. */
    private fun kanalAdi(ham: String?): String {
        var ad = ham?.trim().orEmpty()
        if (ad.isBlank()) return "Kanal"
        while (true) {
            val m = ETIKET.find(ad) ?: break
            val ic = m.groupValues[1]
            val etiket = ic.contains(Regex("\\d+[pi]")) ||
                ic.contains("geo", ignoreCase = true) ||
                ic.contains("24/7") ||
                ic.contains("offline", ignoreCase = true)
            if (!etiket) break
            ad = ad.removeRange(m.range).trim()
        }
        return ad.ifBlank { "Kanal" }
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val gruplar = kanallar()
            .groupBy { grupAdi(it.attributes["group-title"]) }
            .toSortedMap()

        val listeler = gruplar.map { (grup, kanalListesi) ->
            HomePageList(
                grup,
                kanalListesi.map { kanal -> kanal.toSearchResponse() },
                isHorizontalImages = true
            )
        }

        return newHomePageResponse(listeler, hasNext = false)
    }

    override suspend fun search(query: String): List<SearchResponse> =
        kanallar()
            .filter { it.title?.contains(query, ignoreCase = true) == true }
            .map { it.toSearchResponse() }

    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse {
        val veri = fetchDataFromUrlOrJson(url)
        val ayniGrup = kanallar()
            .filter { it.attributes["group-title"] == veri.group && it.title != veri.title }
            .take(20)
            .map { it.toSearchResponse() }

        return newLiveStreamLoadResponse(veri.title, veri.url, url) {
            this.posterUrl       = veri.poster.takeIf { it.isNotBlank() && it != "null" }
            this.plot            = veri.group
            this.tags            = listOfNotNull(veri.group, veri.nation)
            this.recommendations = ayniGrup
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val veri = fetchDataFromUrlOrJson(data)
        Log.d("DunyaTV", "yayin: ${veri.title} -> ${veri.url}")

        callback.invoke(
            newExtractorLink(
                source = this.name,
                name   = veri.title,
                url    = veri.url,
                type   = ExtractorLinkType.M3U8
            ) {
                this.referer = this@DunyaTV.mainUrl
                this.quality = Qualities.Unknown.value
            }
        )

        return true
    }

    private fun PlaylistItem.toSearchResponse(): LiveSearchResponse {
        val adres  = url.orEmpty()
        val baslik = kanalAdi(title).ifBlank { adres }
        val grup   = grupAdi(attributes["group-title"])
        val logo   = attributes["tvg-logo"]?.takeIf { it.isNotBlank() } ?: ""
        val ulke   = attributes["tvg-country"]?.takeIf { it.isNotBlank() } ?: "TR"

        return newLiveSearchResponse(
            baslik,
            LoadData(adres, baslik, logo, grup, ulke).toJson(),
            type = TvType.Live
        ) {
            this.posterUrl = logo.takeIf { it.isNotBlank() }
            this.lang      = "tr"
        }
    }

    data class LoadData(val url: String, val title: String, val poster: String, val group: String, val nation: String)

    private suspend fun fetchDataFromUrlOrJson(data: String): LoadData {
        if (data.startsWith("{")) return parseJson<LoadData>(data)

        val kanal = kanallar().firstOrNull { it.url == data }
            ?: throw IllegalArgumentException("Kanal bulunamadi: $data")

        return LoadData(
            kanal.url.orEmpty(),
            kanalAdi(kanal.title),
            kanal.attributes["tvg-logo"].orEmpty(),
            grupAdi(kanal.attributes["group-title"]),
            kanal.attributes["tvg-country"]?.takeIf { it.isNotBlank() } ?: "TR"
        )
    }

    companion object {
        private val ETIKET = Regex("\\s*[\\[(]([^\\])]*)[\\])]\\s*$")

        private val GRUP_CEVIRI = mapOf(
            "Animation"     to "Animasyon",
            "Auto"          to "Otomobil",
            "Business"      to "Ekonomi",
            "Classic"       to "Klasik",
            "Comedy"        to "Komedi",
            "Cooking"       to "Yemek",
            "Culture"       to "Kültür",
            "Documentary"   to "Belgesel",
            "Education"     to "Eğitim",
            "Entertainment" to "Eğlence",
            "Family"        to "Aile",
            "General"       to "Genel",
            "Kids"          to "Çocuk",
            "Lifestyle"     to "Yaşam",
            "Movies"        to "Film",
            "Music"         to "Müzik",
            "News"          to "Haber",
            "Outdoor"       to "Doğa",
            "Relax"         to "Rahatlama",
            "Religious"     to "Dini",
            "Science"       to "Bilim",
            "Series"        to "Dizi",
            "Shopping"      to "Alışveriş",
            "Sports"        to "Spor",
            "Travel"        to "Gezi",
            "Undefined"     to "Diğer",
            "Weather"       to "Hava Durumu"
        )
    }
}

data class Playlist(
    val items: List<PlaylistItem> = emptyList()
)

data class PlaylistItem(
    val title: String?                  = null,
    val attributes: Map<String, String> = emptyMap(),
    val headers: Map<String, String>    = emptyMap(),
    val url: String?                    = null,
    val userAgent: String?              = null
)

class IptvPlaylistParser {

    fun parseM3U(content: String): Playlist = parseM3U(content.byteInputStream())

    @Throws(PlaylistParserException::class)
    fun parseM3U(input: InputStream): Playlist {
        val reader = input.bufferedReader()

        if (!reader.readLine().isExtendedM3u()) {
            throw PlaylistParserException.InvalidHeader()
        }

        val playlistItems: MutableList<PlaylistItem> = mutableListOf()
        var currentIndex = 0
        var line: String? = reader.readLine()

        while (line != null) {
            if (line.isNotEmpty()) {
                if (line.startsWith(EXT_INF)) {
                    val title      = line.getTitle()
                    val attributes = line.getAttributes()
                    playlistItems.add(PlaylistItem(title, attributes))
                } else if (line.startsWith(EXT_VLC_OPT)) {
                    val item      = playlistItems[currentIndex]
                    val userAgent = item.userAgent ?: line.getTagValue("http-user-agent")
                    val referrer  = line.getTagValue("http-referrer")
                    val headers   = mutableMapOf<String, String>()

                    if (userAgent != null) headers["user-agent"] = userAgent
                    if (referrer != null) headers["referrer"] = referrer

                    playlistItems[currentIndex] = item.copy(userAgent = userAgent, headers = headers)
                } else if (!line.startsWith("#")) {
                    if (currentIndex < playlistItems.size) {
                        val item      = playlistItems[currentIndex]
                        val url       = line.getUrl()
                        val userAgent = line.getUrlParameter("user-agent")
                        val referrer  = line.getUrlParameter("referer")
                        val headers   = if (referrer != null) item.headers + mapOf("referrer" to referrer) else item.headers

                        playlistItems[currentIndex] = item.copy(
                            url       = url,
                            headers   = headers,
                            userAgent = userAgent ?: item.userAgent
                        )
                        currentIndex++
                    }
                }
            }
            line = reader.readLine()
        }
        return Playlist(playlistItems)
    }

    private fun String.replaceQuotesAndTrim(): String = replace("\"", "").trim()

    private fun String.isExtendedM3u(): Boolean = startsWith(EXT_M3U)

    private fun String.getTitle(): String? = split(",").lastOrNull()?.replaceQuotesAndTrim()

    private fun String.getUrl(): String? = split("|").firstOrNull()?.replaceQuotesAndTrim()

    private fun String.getUrlParameter(key: String): String? {
        val urlRegex     = Regex("^(.*)\\|", RegexOption.IGNORE_CASE)
        val keyRegex     = Regex("$key=(\\w[^&]*)", RegexOption.IGNORE_CASE)
        val paramsString = replace(urlRegex, "").replaceQuotesAndTrim()

        return keyRegex.find(paramsString)?.groups?.get(1)?.value
    }

    private fun String.getAttributes(): Map<String, String> {
        val extInfRegex      = Regex("(#EXTINF:.?[0-9]+)", RegexOption.IGNORE_CASE)
        val attributesString = replace(extInfRegex, "").replaceQuotesAndTrim().split(",").first()

        return attributesString
            .split(Regex("\\s"))
            .mapNotNull {
                val pair = it.split("=")
                if (pair.size == 2) pair.first() to pair.last().replaceQuotesAndTrim() else null
            }
            .toMap()
    }

    private fun String.getTagValue(key: String): String? {
        val keyRegex = Regex("$key=(.*)", RegexOption.IGNORE_CASE)
        return keyRegex.find(this)?.groups?.get(1)?.value?.replaceQuotesAndTrim()
    }

    companion object {
        const val EXT_M3U     = "#EXTM3U"
        const val EXT_INF     = "#EXTINF"
        const val EXT_VLC_OPT = "#EXTVLCOPT"
    }
}

sealed class PlaylistParserException(message: String) : Exception(message) {
    class InvalidHeader : PlaylistParserException("Invalid file header. Header doesn't start with #EXTM3U")
}
