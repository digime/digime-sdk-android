package me.digi.saas

import android.app.Application
import me.digi.saas.framework.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class SaasApp : Application() {

    companion object {
        lateinit var instance: SaasApp
            private set
    }

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
        instance = this

        initKoin()
    }

    private fun initKoin() {
        startKoin {
            androidContext(applicationContext)
            modules(
                clientsModule,
                dataAccessModule,
                repositoriesModule,
                useCasesModule,
                viewModelsModule
            )
        }
    }
}