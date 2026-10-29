package com.keyiflerolsun.aciktv

import android.content.Context
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class AcikTVPlugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(AcikTV())
    }
}
