package me.digi.sdk.callbacks

import me.digi.sdk.DMEError
import me.digi.sdk.entities.DMEAccount
import me.digi.sdk.entities.DMEFile
import me.digi.sdk.entities.DMEFileList
import me.digi.sdk.entities.DMESession

typealias DMEAuthorizationCompletion = (session: DMESession?, error: DMEError?) -> Unit

typealias DMEOngoingAuthorizationCompletion = (session: DMESession?, accessToken: String?, refreshToken: String?, error: DMEError?) -> Unit

internal typealias DMEFileListCompletion = (fileList: DMEFileList?, error: DMEError?) -> Unit

typealias DMEFileContentCompletion = (file: DMEFile?, error: DMEError?) -> Unit

typealias DMEAccountsCompletion = (accounts: List<DMEAccount>?, error: DMEError?) -> Unit