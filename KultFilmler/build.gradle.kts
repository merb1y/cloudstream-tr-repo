version = 1016

cloudstream {
    authors     = listOf("keyiflerolsun")
    language    = "tr"
    description = "KÃ¼lt Filmler Ã¶zenle en iyi filmleri derler ve iyi bir altyazÄ±lÄ± film izleme deneyimi sunmayÄ± amaÃ§lar"

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
    **/
    status  = 1 // will be 3 if unspecified
    tvTypes = listOf("Movie", "TvSeries")
    iconUrl = "https://www.google.com/s2/favicons?domain=kultfilmler.net&sz=%size%"
}
