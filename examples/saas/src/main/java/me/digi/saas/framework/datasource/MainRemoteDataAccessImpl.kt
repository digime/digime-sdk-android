package me.digi.saas.framework.datasource

import android.app.Activity
import io.reactivex.rxjava3.core.Single
import me.digi.saas.data.clients.SaasClients
import me.digi.saas.data.remoteaccess.MainRemoteDataAccess
import me.digi.sdk.DMEAuthError
import me.digi.sdk.entities.AuthSession
import me.digi.sdk.entities.DMEFileList

class MainRemoteDataAccessImpl(private val clients: SaasClients) : MainRemoteDataAccess {

    override fun authenticate(activity: Activity): Single<AuthSession> = Single.create { emitter ->
        clients.getPullClient().authorize(activity) { authSession, error ->
            error?.let(emitter::onError)
                ?: (if (authSession != null) emitter.onSuccess(authSession)
                else emitter.onError(DMEAuthError.General()))
        }
    }

    override fun onboardService(activity: Activity, codeValue: String, serviceId: String): Single<Boolean> =
        Single.create { emitter ->
            clients.getPullClient().onboardNew(activity, codeValue, serviceId) { error ->
                error?.let(emitter::onError)
                ?: emitter.onSuccess(true)
            }
        }

    override fun getFileList(): Single<DMEFileList> = Single.create { emitter ->
        clients.getPullClient().getFileList { fileList: DMEFileList?, error ->
            error?.let(emitter::onError)
                ?: if(fileList != null) emitter.onSuccess(fileList)
                    else emitter.onError(DMEAuthError.General())
        }
    }
}