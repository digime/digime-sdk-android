package me.digi.sdk.callbacks

import me.digi.sdk.DMEError
import me.digi.sdk.entities.DMEAccount
import me.digi.sdk.entities.DMEFile

typealias DMEEmptyCompletion = (error: DMEError?) -> Unit