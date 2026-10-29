version = 1019

cloudstream {
    authors     = listOf("keyiflerolsun")
    language    = "tr"
    description = "Film modun geldiyse yÃ¼ksek kalitede en yeni filmleri izle, 1080p izleyebileceÄŸiniz reklamsiz film sitesi."

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
    **/
    status  = 1 // will be 3 if unspecified
    tvTypes = listOf("Movie")
    iconUrl = "https://www.filmmodu.one/icon/favicon.ico"
}
