package com.keyiflerolsun.ardaspor

import android.content.Context
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class ArdaSporPlugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(ArdaSpor())
    }
}
