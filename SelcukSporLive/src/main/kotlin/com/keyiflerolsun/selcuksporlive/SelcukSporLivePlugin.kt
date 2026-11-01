package com.keyiflerolsun.selcuksporlive

import android.content.Context
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class SelcukSporLivePlugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(SelcukSporLive())
    }
}
