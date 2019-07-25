package me.digi.sdk.callbacks

import me.digi.sdk.entities.DMESession

typealias DMEAuthorizationCallback = (session: DMESession?, error: Throwable?) -> Unit