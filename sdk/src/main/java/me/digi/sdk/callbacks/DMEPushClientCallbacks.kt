package me.digi.sdk.callbacks

import me.digi.sdk.entities.DMEPostbox

typealias DMEPostboxCreationCompletion = (DMEPostbox?, error: Throwable?) -> Unit

typealias DMEPostboxPushCompletion = (error: Throwable?) -> Unit