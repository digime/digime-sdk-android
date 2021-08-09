package me.digi.sdk.callbacks

import me.digi.sdk.DMEError
import me.digi.sdk.entities.OngoingPostbox
import me.digi.sdk.entities.Postbox
import me.digi.sdk.entities.response.SaasOngoingPushResponse

typealias DMEPostboxCreationCompletion = (Postbox?, error: DMEError?) -> Unit

typealias DMESaasPostboxOngoingCreationCompletion = (OngoingPostbox?, error: DMEError?) -> Unit

typealias DMEPostboxPushCompletion = (error: DMEError?) -> Unit

typealias DMEOngoingPostboxPushCompletion = (SaasOngoingPushResponse?, error: DMEError?) -> Unit