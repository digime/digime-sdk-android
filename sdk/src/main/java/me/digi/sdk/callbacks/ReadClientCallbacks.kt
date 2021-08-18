package me.digi.sdk.callbacks

import me.digi.sdk.Error
import me.digi.sdk.entities.DMEAccount
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.response.*
import me.digi.sdk.entities.service.ServicesResponse

// TODO: Unused
typealias DMEAuthorizationCompletion = (session: SessionResponse?, error: Error?) -> Unit

typealias AuthorizationCompletion = (consentAuthResponse: ConsentAuthResponse?, error: Error?) -> Unit

// TODO: Unused
typealias AuthCompletion = (authResponse: AuthorizeResponse?, error: Error?) -> Unit

typealias ServiceOnboardingCompletion = (error: Error?) -> Unit

typealias FileListCompletion = (fileList: DMEFileList?, error: Error?) -> Unit

typealias FileContentCompletion = (file: DMEFile?, error: Error?) -> Unit

// TODO: Unused - kinda
typealias DMEAccountsCompletion = (accounts: List<DMEAccount>?, error: Error?) -> Unit

typealias IncrementalFileListUpdate = (fileList: DMEFileList, updatedFileIds: List<String>) -> Unit

// TODO: Unused
typealias DMESaasOngoingAuthorizationCompletion = (credentials: CredentialsPayload?, error: Error?) -> Unit

typealias AvailableServicesCompletion = (ServicesResponse?, error: Error?) -> Unit

typealias UserDeleteCompletion = (isLibraryDeleted: Boolean?, error: Error?) -> Unit

// TODO: Unused
typealias GetSessionCompletion = (isSessionUpdated: Boolean?, error: Error?) -> Unit

typealias GetAuthorizationDoneCompletion = (response: AuthorizationResponse?, error: Error?) -> Unit