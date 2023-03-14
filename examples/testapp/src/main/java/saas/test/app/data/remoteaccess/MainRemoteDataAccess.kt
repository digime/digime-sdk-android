package saas.test.app.data.remoteaccess

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.DataRequest
import me.digi.sdk.entities.WriteDataPayload
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.response.*
import me.digi.sdk.entities.service.Service
import okhttp3.ResponseBody

interface MainRemoteDataAccess {

    fun onboardService(
        activity: Activity,
        serviceId: String,
        accessToken: String
    ): Single<Boolean>

    fun getFileList(): Single<FileList>
    fun getRawFileList(): Single<FileList>
    fun getServicesForContract(contractId: String): Single<List<Service>>
    fun pushDataToPostbox(
        payloadToWrite: WriteDataPayload,
        accessToken: String
    ): Single<DataWriteResponse>

    fun deleteUsersLibrary(accessToken: String?): Single<Boolean>

    fun authorizeAccess(
        activity: Activity,
        contractType: String,
        scope: DataRequest?,
        credentials: CredentialsPayload?,
        serviceId: String?
    ): Single<AuthorizationResponse>

    fun getFile(fileName: String): Single<FileItemBytes>

    fun getFileBytes(fileName: String): Single<FileItemBytes>

    fun getAccounts(): Single<ReadAccountsResponse>
}