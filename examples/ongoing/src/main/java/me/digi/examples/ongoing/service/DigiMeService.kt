package me.digi.examples.ongoing.service

import android.app.Activity
import android.app.Application
import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.SingleTransformer
import me.digi.examples.ongoing.model.Song
import me.digi.examples.ongoing.utils.FileUtils
import me.digi.examples.ongoing.utils.authorizeOngoingAccess
import me.digi.examples.ongoing.utils.getSessionData
import me.digi.ongoing.R
import me.digi.sdk.DMEPullClient
import me.digi.sdk.entities.*
import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class DigiMeService(private val context: Application) {

    companion object {
        private const val SHAREDPREFS_KEY = "DigiMeXGenrefySharedPreferences"
        private const val CACHED_CREDENTIAL_KEY = "CachedCredential"
    }

    private val client: DMEPullClient by lazy {
        val privateKey = DMECryptoUtilities(context).privateKeyHexFrom(
            context.getString(R.string.digime_p12_filename),
            context.getString(R.string.digime_p12_password)
        )
        val configuration = DMEPullConfiguration(
            context.getString(R.string.digime_application_id),
            context.getString(R.string.digime_contract_id),
            privateKey
        )
        DMEPullClient(context, configuration)
    }

    private val gsonAgent: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(Song::class.java, Song.Adapter())
            .create()
    }

    fun obtainAccessRights(activity: Activity) = client.authorizeOngoingAccess(activity, createScopeForDailyPlayHistory(), getCachedCredential())
        .map { it.second }
        .compose(cacheCredential())
        .flatMapCompletable { Completable.complete() }

    fun fetchData() = client.getSessionData()
        .map { gsonAgent.fromJson<List<Song>>(String(it.fileContent), object: TypeToken<List<Song>>() {}.type) }
        .flatMapIterable { it }
        .filter { TimeUnit.DAYS.convert(abs(Date().time - it.createdDate), TimeUnit.MILLISECONDS) <= 24 }

    private fun createScopeForDailyPlayHistory(): DMEScope {
        val objects = listOf(DMEServiceObjectType(406))
        val services = listOf(DMEServiceType(19, objects))
        val groups = listOf(DMEServiceGroup(5, services))
        return DMEScope().apply {
            serviceGroups = groups
            timeRanges = listOf(DMETimeRange(to = null, from = null, last = "1d", type = null))
        }
    }

    fun getCachedCredential() =
        context.getSharedPreferences(SHAREDPREFS_KEY, Context.MODE_PRIVATE).run {
            getString(CACHED_CREDENTIAL_KEY, null)?.let {
                Gson().fromJson(it, DMEOAuthToken::class.java)
            }
        }

    private fun cacheCredential() = SingleTransformer<DMEOAuthToken, DMEOAuthToken> {
        it.map { credential ->
            credential.apply {
                context.getSharedPreferences(SHAREDPREFS_KEY, Context.MODE_PRIVATE).edit().run {
                    val encodedCredential = Gson().toJson(credential)
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