version = 1002

cloudstream {
    authors     = listOf("nikyokki")
    language    = "tr"
    description = "DMAX dizi ve programlarÄ±nÄ± takip etmek iÃ§in ziyaret edin. DMAX dizi ve programlarÄ±nÄ± tek parÃ§a ve hd kalitesiyle izleyin."

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
    **/
    status  = 1 // will be 3 if unspecified
    tvTypes = listOf("TvSeries")
    iconUrl = "https://www.dmax.com.tr/favicon.ico"
}