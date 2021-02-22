package me.digi.ongoingpostbox

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class OngoingPostboxApp: Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        initKoin()
    }

    private fun initKoin() {
        startKoin {
            androidContext(applicationContext)
            modules()
        }
    }
}