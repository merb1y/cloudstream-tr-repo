package com.keyiflerolsun.dunyatv

import android.content.Context
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class DunyaTVPlugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(DunyaTV())
    }
}
