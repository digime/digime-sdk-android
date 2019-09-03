package me.digi.sdk.callbacks

import me.digi.sdk.DMEError
import me.digi.sdk.entities.DMEAccount
import me.digi.sdk.entities.DMEFile
import me.digi.sdk.entities.DMESession

typealias DMEAuthorizationCompletion = (session: DMESession?, error: DMEError?) -> Unit

typealias DMEFileListCompletion = (fileIds: List<String>?, error: DMEError?) -> Unit

typealias DMEFileContentCompletion = (file: DMEFile?, error: DMEError?) -> Unit

typealias DMEAccountsCompletion = (accounts: List<DMEAccount>?, error: DMEError?) -> Unit