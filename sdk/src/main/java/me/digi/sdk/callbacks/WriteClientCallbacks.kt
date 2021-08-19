package me.digi.sdk.callbacks

import me.digi.sdk.Error
import me.digi.sdk.entities.Data
import me.digi.sdk.entities.OngoingData
import me.digi.sdk.entities.response.OngoingWriteResponse

// TODO: Unused
typealias PostboxCreationCompletion = (Data?, error: Error?) -> Unit

// TODO: Unused
typealias SaasPostboxOngoingCreationCompletion = (OngoingData?, error: Error?) -> Unit

// TODO: Unused
typealias PostboxPushCompletion = (error: Error?) -> Unit

typealias OngoingWriteCompletion = (OngoingWriteResponse?, error: Error?) -> Unit