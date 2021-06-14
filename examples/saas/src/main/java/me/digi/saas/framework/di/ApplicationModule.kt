package me.digi.saas.framework.di

import me.digi.saas.R
import me.digi.sdk.DMEPullClient
import me.digi.sdk.entities.DMEPullConfiguration
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val applicationModule = module {

    single {
        val configuration = DMEPullConfiguration(
            androidContext().getString(R.string.digime_application_id),
            androidContext().getString(R.string.digime_contract_id),
            androidContext().getString(R.string.digime_private_key)
        )

        DMEPullClient(androidContext().applicationContext, configuration)
    }
}