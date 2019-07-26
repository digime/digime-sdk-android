package me.digi.sdk.callbacks

import me.digi.sdk.DMEError
import me.digi.sdk.entities.DMEPostbox

typealias DMEPostboxCreationCompletion = (DMEPostbox?, error: DMEError?) -> Unit

typealias DMEPostboxPushCompletion = (error: DMEError?) -> Unit