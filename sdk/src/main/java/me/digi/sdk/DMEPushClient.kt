package me.digi.sdk

import android.content.Context
import me.digi.sdk.callbacks.DMEPostboxCreationCompletion
import me.digi.sdk.entities.DMEClientConfiguration

class DMEPushClient(val context: Context, val configuration: DMEClientConfiguration): DMEClient(context, configuration) {

    fun createPostbox(completion: DMEPostboxCreationCompletion) {

        if (!sessionManager.isSessionValid())
            throw DMEAuthError.InvalidSession()

    }
}