package me.digi.sdk.callbacks

import me.digi.sdk.DMEError
import me.digi.sdk.entities.*

typealias DMEAuthorizationCompletion = (session: DMESession?, error: DMEError?) -> Unit

typealias DMEOngoingAuthorizationCompletion = (session: DMESession?, credentials: DMEOAuthToken?, error: DMEError?) -> Unit

typealias DMEFileListCompletion = (fileList: DMEFileList?, error: DMEError?) -> Unit

typealias DMEFileContentCompletion = (file: DMEFile?, error: DMEError?) -> Unit

typealias DMEAccountsCompletion = (accounts: List<DMEAccount>?, error: DMEError?) -> Unit

typealias DMEIncrementalFileListUpdate = (fileList: DMEFileList, updatedFileIds: List<String>) -> Unit