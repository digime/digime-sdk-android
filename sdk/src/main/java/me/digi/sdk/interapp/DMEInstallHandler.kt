package me.digi.sdk.interapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import me.digi.sdk.R

class DMEInstallHandler: BroadcastReceiver() {

    private var installCallback: (() -> Unit)? = null

    companion object {

        fun registerNewInstallHandler(installCallback: () -> Unit): DMEInstallHandler {
            val receiver = DMEInstallHandler()
            val applicationContext = DMEAppCommunicator.getSharedInstance().context

            val filter = IntentFilter(Intent.ACTION_PACKAGE_ADDED)
            filter.addDataScheme("package")

            receiver.installCallback = installCallback

            applicationContext.registerReceiver(receiver, filter)

            return receiver
        }
    }


    override fun onReceive(context: Context?, intent: Intent?) {
        val intentAction = intent?.action
        val installedPackage = intent?.data?.schemeSpecificPart

        val digimePackageName = context?.getString(R.string.const_digime_app_package_name) ?: ""

        if (intentAction == Intent.ACTION_PACKAGE_ADDED && installedPackage == digimePackageName) {
            installCallback?.invoke()
            unregister()
        }
    }

    fun unregister() {
        val applicationContext = DMEAppCommunicator.getSharedInstance().context
        applicationContext.unregisterReceiver(this)
    }
}