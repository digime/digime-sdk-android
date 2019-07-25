package me.digi.sdk

import android.content.Context
import me.digi.sdk.entities.DMEClientConfiguration
import me.digi.sdk.utilities.DMESessionManager

internal abstract class DMEClient(private val context: Context, private val configuration: DMEClientConfiguration) {

    val sessionManager: DMESessionManager

}