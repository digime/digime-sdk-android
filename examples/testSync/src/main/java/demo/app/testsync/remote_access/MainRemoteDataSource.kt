package me.digi.app.remote_access

import android.app.Activity
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.DataRequest
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.response.*
import me.digi.sdk.entities.service.Service

interface MainRemoteDataSource {

    fun onboardService(
        activity: Activity,
        serviceId: String,
        accessToken: String
    ): Single<Boolean>

    fun getServicesForContract(contractId: String): Single<List<Service>>

    fun authorizeAccess(
        activity: Activity,
        scope: DataRequest?,
        credentials: CredentialsPayload?,
        serviceId: String?
    ): Single<AuthorizationResponse>

    fun getSessionData(accessToken: String, scope: DataRequest? = null): Observable<FileItem>

    fun readFileList(accessToken: String): Single<FileList>

    fun getUserAccounts(accessToken: String): Single<ReadAccountsResponse>

    fun getFile(fileName: String, accessToken: String): Single<FileItemBytes>

    fun getActiveDownloadCount() : Int

    fun getSyncStatus() : FileList.SyncStatus?

    fun deleteLibrary(accessToken: String): Single<Boolean>
}