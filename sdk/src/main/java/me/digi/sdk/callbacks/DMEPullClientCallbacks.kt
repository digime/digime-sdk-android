package me.digi.sdk.callbacks

import me.digi.sdk.DMEError
import me.digi.sdk.entities.DMESession

typealias DMEAuthorizationCompletion = (session: DMESession?, error: DMEError?) -> Unit