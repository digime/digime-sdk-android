package me.digi.sdk.callbacks

import me.digi.sdk.Error
import me.digi.sdk.entities.Data
import me.digi.sdk.entities.OngoingData
import me.digi.sdk.entities.response.OngoingWriteResponse

// TODO: Unused
typealias DMEPostboxCreationCompletion = (Data?, error: Error?) -> Unit

// TODO: Unused
typealias DMESaasPostboxOngoingCreationCompletion = (OngoingData?, error: Error?) -> Unit

// TODO: Unused
typealias DMEPostboxPushCompletion = (error: Error?) -> Unit

typealias OngoingWriteCompletion = (OngoingWriteResponse?, error: Error?) -> Unit