package me.digi.sdk.saas.repositories

import android.util.Base64
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.digi.sdk.api.DMEAPIClient
import me.digi.sdk.entities.DMEFileList
import me.digi.sdk.entities.DMEPullConfiguration
import me.digi.sdk.entities.Payload
import me.digi.sdk.saas.utils.Resource
import me.digi.sdk.saas.utils.safeCall
import me.digi.sdk.utilities.DMESessionManager
import me.digi.sdk.utilities.crypto.DMEByteTransformer
import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import me.digi.sdk.utilities.crypto.DMEKeyTransformer
import me.digi.sdk.utilities.jwt.DMEPreauthorizationRequestJWT

class DefaultMainRepository : MainRepository {

    override suspend fun fetchPreAuthorizationCode(
        configuration: DMEPullConfiguration,
        apiClient: DMEAPIClient,
        sessionManager: DMESessionManager
    ): Resource<Payload> =
        withContext(Dispatchers.IO) {
            safeCall {
                val codeVerifier =
                    DMEByteTransformer.hexStringFromBytes(DMECryptoUtilities.generateSecureRandom(64))

                val jwt = DMEPreauthorizationRequestJWT(
                    configuration.appId,
                    configuration.contractId,
                    codeVerifier
                )

                val authHeader =
                    jwt.sign(DMEKeyTransformer.privateKeyFromString(configuration.privateKeyHex))
                        .tokenize()

                val response = apiClient.argonService.fetchPreAuthorizationCode(authHeader)
                sessionManager.currentSession = response.session

                val chunks: List<String> = response.token.split(".")
                val payloadJson: String = String(Base64.decode(chunks[1], Base64.URL_SAFE))
                val payload = Gson().fromJson(payloadJson, Payload::class.java)

                Resource.Success(payload)
            }
        }

    override suspend fun getFileList(
        apiClient: DMEAPIClient,
        sessionKey: String
    ): Resource<DMEFileList> =
        withContext(Dispatchers.IO) {
            safeCall {
                val response: DMEFileList =
                    apiClient.argonService.getFileListForServices(sessionKey)

                Resource.Success(response)
            }
        }
}