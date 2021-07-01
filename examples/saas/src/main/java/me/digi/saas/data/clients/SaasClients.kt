package me.digi.saas.data.clients

import me.digi.sdk.DMEPullClient
import me.digi.sdk.DMEPushClient

interface SaasClients {
    fun getPullClient(): DMEPullClient
    fun getPushClient() : DMEPushClient
}