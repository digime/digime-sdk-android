package me.digi.examples.ongoing.service

import android.app.Activity
import android.app.Application
import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.SingleTransformer
import me.digi.examples.ongoing.model.Song
import me.digi.examples.ongoing.utils.FileUtils
import me.digi.examples.ongoing.utils.authorizeOngoingAccess
import me.digi.examples.ongoing.utils.getSessionData
import me.digi.ongoing.R
import me.digi.sdk.entities.*
import me.digi.sdk.entities.payload.AccessToken
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.payload.RefreshToken
import me.digi.sdk.entities.response.AuthorizationResponse
import me.digi.sdk.unify.DigiMe
import me.digi.sdk.unify.DigiMeConfiguration
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class DigiMeService(private val context: Application) {

    companion object {
        private const val SHARED_PREFS_KEY = "DigiMeXGenrefySharedPreferences"
        private const val CACHED_CREDENTIAL_KEY = "CachedCredential"
    }

    private val client: DigiMe by lazy {

        val configuration = DigiMeConfiguration(
            context.getString(R.string.staging_app_id),
            context.getString(R.string.staging_contract_id),
            context.getString(R.string.staging_private_key)
        )

        configuration.baseUrl = "https://api.stagingdigi.me/"

        DigiMe(context, configuration)
    }

    private val gsonAgent: Gson by lazy { GsonBuilder().create() }

    fun obtainAccessRights(activity: Activity): Completable = client.authorizeOngoingAccess(
        activity,
        scope = createScopeForDailyPlayHistory(),
        credentials = getCachedCredential(),
        serviceId = "16" // Spotify
    )
        .map { it }
        .compose(cacheCredentials())
        .flatMapCompletable { Completable.complete() }

    fun fetchData(): Observable<Song> = client.getSessionData()
        .map {
            gsonAgent.fromJson<List<Song>>(
                it.fileContent,
                object : TypeToken<List<Song>>() {}.type
            )
        }
        .flatMapIterable { it }
        .filter {
            TimeUnit.HOURS.convert(
                abs(Date().time - it.createdDate),
                TimeUnit.MILLISECONDS
            ) <= 24
        }

    private fun createScopeForDailyPlayHistory(): CaScope {
        val objects: List<ServiceObjectType> = listOf(ServiceObjectType(id = 406))
        val services: List<ServiceType> = listOf(ServiceType(id = 19, serviceObjectTypes = objects))
        val groups: List<ServiceGroup> = listOf(ServiceGroup(id = 5, serviceTypes = services))
        val timeRanges: List<TimeRange> =
            listOf(TimeRange(to = null, from = null, last = "1d", type = null))
        return CaScope(serviceGroups = groups, timeRanges = timeRanges)
    }

    fun getCachedCredential(): CredentialsPayload? =
        context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_CREDENTIAL_KEY, null)?.let {
                Gson().fromJson(it, CredentialsPayload::class.java)
            }
        }

    private fun cacheCredentials(): SingleTransformer<in AuthorizationResponse, out AuthorizationResponse> =
        SingleTransformer<AuthorizationResponse, AuthorizationResponse> {
            it.map { response ->
                response.apply {
                    context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE).edit()
                        .run {

                            val credentials = CredentialsPayload().copy(
                                accessToken = AccessToken(value = response.credentials?.accessToken),
                                refreshToken = RefreshToken(value = response.credentials?.refreshToken)
                            )

                            val encodedCredential = Gson().toJson(credentials)
                            putString(CACHED_CREDENTIAL_KEY, encodedCredential)

                            apply()
                        }
                }
            }
        }

    fun getCachedSongs() = FileUtils(context).listFilesInCache()
        .map { it.readBytes() }
        .map { String(it, StandardCharsets.UTF_8) }
        .map { Gson().fromJson(it, Song::class.java) }

    fun cacheSongs(songs: List<Song>) {
        songs.forEach {
            val encoded = Gson().toJson(it).toByteArray(StandardCharsets.UTF_8)
            FileUtils(context).storeBytes(encoded, it.entityId)
        }
    }
}