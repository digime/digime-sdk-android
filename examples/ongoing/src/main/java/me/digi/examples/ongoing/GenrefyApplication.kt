package me.digi.examples.ongoing

import android.app.Application
import me.digi.examples.ongoing.service.DigiMeService

class GenrefyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DigiMeService.configureSdk(this)
    }

}