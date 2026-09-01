package com.tealium.prism.example

import android.app.Application

class ExampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        TealiumHelper.init(this)
    }
}
