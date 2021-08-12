package me.digi.sdk.callbacks

import me.digi.sdk.DMEError
import me.digi.sdk.entities.DMEAccount
import me.digi.sdk.entities.DMEFile
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.response.*
import me.digi.sdk.entities.service.ServicesResponse

typealias DMEAuthorizationCompletion = (session: SessionResponse?, error: DMEError?) -> Unit

typealias AuthorizationCompletion = (consentAuthResponse: ConsentAuthResponse?, error: DMEError?) -> Unit

typealias AuthCompletion = (authResponse: AuthorizeResponse?, error: DMEError?) -> Unit

typealias OnboardingCompletion = (error: DMEError?) -> Unit

typealias DMEFileListCompletion = (fileList: DMEFileList?, error: DMEError?) -> Unit

typealias DMEFileContentCompletion = (file: DMEFile?, error: DMEError?) -> Unit

typealias DMEAccountsCompletion = (accounts: List<DMEAccount>?, error: DMEError?) -> Unit

typealias DMEIncrementalFileListUpdate = (fileList: DMEFileList, updatedFileIds: List<String>) -> Unit

typealias DMESaasOngoingAuthorizationCompletion = (credentials: CredentialsPayload?, error: DMEError?) -> Unit

typealias DMEServicesForContract = (ServicesResponse?, error: DMEError?) -> Unit

typealias DMEUserLibraryDeletion = (isLibraryDeleted: Boolean?, error: DMEError?) -> Unit

typealias GetSessionCompletion = (isSessionUpdated: Boolean?, error: DMEError?) -> Unit

typealias GetAuthorizationDoneCompletion = (response: AuthorizationResponse?, error: DMEError?) -> Unit