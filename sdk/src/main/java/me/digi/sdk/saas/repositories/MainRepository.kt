package me.digi.sdk.saas.repositories

import me.digi.sdk.api.DMEAPIClient
import me.digi.sdk.entities.DMEFileList
import me.digi.sdk.entities.DMEPullConfiguration
import me.digi.sdk.entities.Payload
import me.digi.sdk.saas.utils.Resource
import me.digi.sdk.utilities.DMESessionManager

interface MainRepository {

    suspend fun fetchPreAuthorizationCode(
        configuration: DMEPullConfiguration,
        apiClient: DMEAPIClient,
        sessionManager: DMESessionManager
    ): Resource<Payload>

    suspend fun getFileList(apiClient: DMEAPIClient, sessionKey: String): Resource<DMEFileList>
}