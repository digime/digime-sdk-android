package me.digi.examples.ongoing

import android.app.Application
import me.digi.examples.ongoing.service.DigiMeService
import me.digi.examples.ongoing.service.ObjectBox

class GenrefyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ObjectBox.init(this)
        DigiMeService.configureSdk(this)
    }

}