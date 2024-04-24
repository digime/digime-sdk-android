package me.digi.app.remote_access

import android.app.Activity
import android.content.Context
import demo.app.testsync.R
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.AuthError
import me.digi.sdk.Error
import me.digi.sdk.Init
import me.digi.sdk.entities.DataRequest
import me.digi.sdk.entities.configuration.DigiMeConfiguration
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.response.*
import me.digi.sdk.entities.service.Service


class MainRemoteDataSourceImpl(private val context: Context) : MainRemoteDataSource {

    private val client: Init by lazy {

        val configuration = DigiMeConfiguration(
            appId = context.getString(R.string.appId),
            contractId = context.getString(R.string.contractId),
            privateKeyHex = context.getString(R.string.privateKey)
        )

        Init(context, configuration)
    }

    override fun onboardService(
        activity: Activity,
        serviceId: String,
        accessToken: String
    ): Single<Boolean> =
        Single.create { emitter ->
            client.addService(activity, serviceId, accessToken) { error ->
                error?.let(emitter::onError) ?: emitter.onSuccess(true)
            }
        }

    override fun getServicesForContract(contractId: String): Single<List<Service>> =
        Single.create { emitter ->
            client.getAvailableServices(null) { servicesResponse, error ->
                error?.let(emitter::onError)
                    ?: emitter.onSuccess(servicesResponse?.data?.services as List<Service>)
            }
        }

    override fun authorizeAccess(
        activity: Activity,
        scope: DataRequest?,
        credentials: CredentialsPayload?,
        serviceId: String?
    ): Single<AuthorizationResponse> =
        Single.create { emitter ->
            client.authorizeAccess(
                fromActivity = activity,
                scope = scope,
                credentials = credentials,
                serviceId = serviceId
            ) { response, error ->
                error?.let(emitter::onError)
                    ?: emitter.onSuccess(response as @NonNull AuthorizationResponse)
            }
        }

    override fun getSessionData(accessToken: String, scope: DataRequest?): Observable<FileItem> =
        Observable.create { emitter ->
            client.readAllFiles(
                scope,
                accessToken,
                { file, _ ->
                    file?.let {
                        emitter.onNext(it)
                    }
                }) { _, error ->
                error?.let(emitter::onError) ?: run {
                    emitter.onComplete()
                }
            }
        }

    override fun readFileList(accessToken: String): Single<FileList> =
        Single.create { emitter ->
            client.readFileList(accessToken) { fileList: FileList?, error: Error? ->
                error?.let(emitter::onError)
                    ?: emitter.onSuccess(fileList!!)
            }
        }

    override fun getUserAccounts(accessToken: String): Single<ReadAccountsResponse> =
        Single.create { emitter ->
            client.readAccounts(accessToken) { accounts: ReadAccountsResponse?, error: Error? ->
                error?.let(emitter::onError)
                    ?: emitter.onSuccess(accounts as @NonNull ReadAccountsResponse)
            }
        }

    override fun getFile(fileName: String, accessToken: String): Single<FileItemBytes> =
        Single.create { emitter ->
            client.readFile(
                accessToken,
                fileName,
            ) { file, error ->
                error?.let(emitter::onError)
                    ?: (if (file != null) emitter.onSuccess(file) else emitter.onError(AuthError.General()))
            }
        }

    override fun deleteLibrary(accessToken: String): Single<Boolean> =
        Single.create { emitter ->
            client.deleteUser(
                accessToken
            ) { file, error ->
                error?.let(emitter::onError)
                    ?: (if (file != null) emitter.onSuccess(file) else emitter.onError(AuthError.General()))
            }
        }

    override fun getActiveDownloadCount(): Int {
        return client.activeDownloadCount
    }

    override fun getSyncStatus(): FileList.SyncStatus? {
        return client.activeSyncStatus
    }
}