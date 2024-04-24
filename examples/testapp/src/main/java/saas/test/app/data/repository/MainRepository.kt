package saas.test.app.data.repository

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.sdk.entities.WriteDataPayload
import me.digi.sdk.entities.DataRequest
import me.digi.sdk.entities.response.*
import me.digi.sdk.entities.service.Service
import okhttp3.ResponseBody

interface MainRepository {
    fun getFileList(): Single<FileList>
    fun getRawFileList(): Single<FileList>
    fun onboardService(
        activity: Activity,
        serviceId: String,
        accessToken: String
    ): Single<Boolean>

    fun getServicesForContract(contractId: String): Single<List<Service>>
    fun pushDataToPostbox(
        payloadWrite: WriteDataPayload,
        accessToken: String
    ): Single<DataWriteResponse>

    fun deleteUsersLibrary(): Single<Boolean>

    fun authorizeAccess(
        activity: Activity,
        contractType: String,
        scope: DataRequest?,
        serviceId: String?,
    ): Single<AuthorizationResponse>

    fun getFile(fileName: String): Single<FileItemBytes>

    fun getFileBytes(fileName: String): Single<FileItemBytes>

    fun getAccounts(): Single<ReadAccountsResponse>
}