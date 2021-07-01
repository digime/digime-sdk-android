package me.digi.saas.data.clients

import android.content.Context
import me.digi.saas.R
import me.digi.saas.framework.utils.AppConst
import me.digi.sdk.DMEPullClient
import me.digi.sdk.DMEPushClient
import me.digi.sdk.entities.DMEPullConfiguration
import me.digi.sdk.entities.DMEPushConfiguration

class SaasClientsImpl(private val context: Context): SaasClients {

    override fun getPullClient(): DMEPullClient {

        val configuration = DMEPullConfiguration(
            context.getString(R.string.appId),
            context.getString(R.string.pullContractId),
            context.getString(R.string.pullContractPrivateKey)
        )

        configuration.baseUrl = AppConst.BASE_URL

        return DMEPullClient(context, configuration)
    }

    override fun getPushClient(): DMEPushClient {

        val configuration = DMEPushConfiguration(
            context.getString(R.string.appId),
            context.getString(R.string.pushContractId),
            context.getString(R.string.pushContractPrivateKey)
        )

        configuration.baseUrl = AppConst.BASE_URL

        return DMEPushClient(context, configuration)
    }
}