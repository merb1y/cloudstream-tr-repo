package com.ornek.repo

import android.content.Context
import com.lagradost.cloudstream3.HomePageList
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newMovieSearchResponse
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.newExtractorLink

/**
 * CloudStream, eklentiyi bu sınıf üzerinden yükler.
 * Yeni bir sağlayıcı eklemek için registerMainAPI(...) çağrısını çoğaltman yeterli.
 */
@CloudstreamPlugin
class OrnekDiziPlugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(OrnekDizi())
    }
}

/**
 * Örnek sağlayıcı.
 *
 * Gerçek bir siteyi çekmek için üç yeri doldurman gerekiyor:
 *  1) [getMainPage]  -> sitenin ana sayfasındaki listeleri çek (Jsoup ile HTML ayrıştır)
 *  2) [search]       -> sitenin arama sayfasını çek
 *  3) [load] + [loadLinks] -> detay sayfasından oynatma linklerini çıkar
 *
 * Şu an demo veriyle çalışıyor; böylece repo kurulumunu ve oynatmayı
 * gerçek site olmadan test edebilirsin.
 */
class OrnekDizi : MainAPI() {
    override var mainUrl = "https://ornek.local"
    override var name = "Örnek Dizi"
    override var lang = "tr"

    override val hasMainPage = true
    override val supportedTypes = setOf(TvType.Movie)

    private data class Demo(val title: String, val stream: String, val poster: String)

    private val demolar = listOf(
        Demo(
            "Big Buck Bunny",
            "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
            "https://peach.blender.org/wp-content/uploads/title_anouncement.jpg",
        ),
        Demo(
            "Sintel",
            "https://test-streams.mux.dev/pts_shift/master.m3u8",
            "https://durian.blender.org/wp-content/uploads/2022/09/sintel_poster.jpg",
        ),
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val items = demolar.mapIndexed { index, demo ->
            newMovieSearchResponse(
                demo.title,
                "$mainUrl/demo/$index",
                TvType.Movie,
                fix = false,
            ) {
                this.posterUrl = demo.poster
            }
        }
        return newHomePageResponse(HomePageList("Örnek içerikler", items, isHorizontalImages = true))
    }

    override suspend fun search(query: String): List<SearchResponse> {
        return demolar.mapIndexedNotNull { index, demo ->
            if (!demo.title.contains(query, ignoreCase = true)) return@mapIndexedNotNull null
            newMovieSearchResponse(demo.title, "$mainUrl/demo/$index", TvType.Movie, fix = false) {
                this.posterUrl = demo.poster
            }
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val index = url.substringAfterLast('/').toIntOrNull() ?: return null
        val demo = demolar.getOrNull(index) ?: return null

        return newMovieLoadResponse(demo.title, url, TvType.Movie, dataUrl = demo.stream) {
            this.posterUrl = demo.poster
            this.plot = "Kendi reponuzun kurulumunu test etmek için hazırlanmış örnek kayıt."
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        // Gerçek bir eklentide burada site içindeki player iframe'i çözülür.
        callback(
            newExtractorLink(
                source = name,
                name = "Demo akış",
                url = data,
                type = ExtractorLinkType.M3U8,
            ) {
                this.referer = ""
            }
        )
        return true
    }
}
