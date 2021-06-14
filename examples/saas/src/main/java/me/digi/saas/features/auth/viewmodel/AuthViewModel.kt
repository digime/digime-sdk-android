package me.digi.saas.features.auth.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import me.digi.sdk.DMEError
import me.digi.sdk.DMEPullClient
import me.digi.sdk.entities.DMEScope

private const val TAG = "AuthViewModel"

class AuthViewModel(private val client: DMEPullClient) : ViewModel() {

    private var test : DMEScope? = null

    fun authorize(fromActivity: Activity) {
        client.authorize(fromActivity, test) { authSession, error: DMEError? ->
            Log.d(TAG, "${authSession?.code}")
        }
    }
}