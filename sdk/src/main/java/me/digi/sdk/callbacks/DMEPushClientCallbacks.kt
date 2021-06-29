package me.digi.sdk.callbacks

import me.digi.sdk.DMEError
import me.digi.sdk.entities.*

typealias DMEPostboxCreationCompletion = (DMEPostbox?, error: DMEError?) -> Unit

typealias DMEPostboxAuthCompletion = (session: DMESession?, postbox: DMEPostbox?, error: DMEError?) -> Unit

typealias DMEPostboxOngoingCreationCompletion = (DMEPostbox?, credentials: DMEOAuthToken?, error: DMEError?) -> Unit

typealias DMESaasPostboxOngoingCreationCompletion = (DMEOngoingPostboxData?, credentials: DMETokenExchange?, error: DMEError?) -> Unit

typealias DMEPostboxPushCompletion = (error: DMEError?) -> Unit

typealias DMEOngoingPostboxPushCompletion = (isDataPushSuccessful: Boolean, error: DMEError?) -> Unit