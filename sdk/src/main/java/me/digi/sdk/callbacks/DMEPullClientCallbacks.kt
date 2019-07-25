package me.digi.sdk.callbacks

import me.digi.sdk.entities.DMESession

typealias DMEPullClientCallbacks = (session: DMESession, error: Throwable) -> Unit