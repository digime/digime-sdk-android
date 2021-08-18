package me.digi.sdk.callbacks

import me.digi.sdk.Error
import me.digi.sdk.entities.Data
import me.digi.sdk.entities.OngoingPostbox
import me.digi.sdk.entities.response.OngoingWriteResponse

typealias DMEPostboxCreationCompletion = (Data?, error: Error?) -> Unit

typealias DMESaasPostboxOngoingCreationCompletion = (OngoingPostbox?, error: Error?) -> Unit

typealias DMEPostboxPushCompletion = (error: Error?) -> Unit

typealias OngoingWriteCompletion = (OngoingWriteResponse?, error: Error?) -> Unit