/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */
package me.digi.sdk.core

import android.app.Activity
import android.content.Intent
import me.digi.sdk.core.internal.ipc.DigiMeDirectResolver
import me.digi.sdk.core.session.CASession

class DigiMeGuestCAAuthManager: DigiMeBaseAuthManager<CASession>() {

    override fun getRequestCode() = 764

    override fun createAppIntent(intentSession: CASession?) = Intent() // Not required

    override fun handleSuccess(data: Intent?) {
        callback.succeeded(SDKResponse(extractSession(), null))
    }

    override fun canHandleAuthImmediately(activity: Activity) = true

    override fun createResolver(activity: Activity) = DigiMeDirectResolver()

}