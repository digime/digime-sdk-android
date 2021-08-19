package me.digi.sdk.callbacks

import me.digi.sdk.Error
import me.digi.sdk.entities.Account
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.response.*
import me.digi.sdk.entities.service.ServicesResponse

// TODO: Unused
typealias DMEAuthorizationCompletion = (session: SessionResponse?, error: Error?) -> Unit

typealias AuthorizationCompletion = (consentAuthResponse: ConsentAuthResponse?, error: Error?) -> Unit

// TODO: Unused
typealias AuthCompletion = (authResponse: AuthorizeResponse?, error: Error?) -> Unit

typealias ServiceOnboardingCompletion = (error: Error?) -> Unit

typealias FileListCompletion = (fileList: FileList?, error: Error?) -> Unit

typealias FileContentCompletion = (file: File?, error: Error?) -> Unit

// TODO: Unused - kinda
typealias AccountsCompletion = (accounts: List<Account>?, error: Error?) -> Unit

typealias IncrementalFileListUpdate = (fileList: FileList, updatedFileIds: List<String>) -> Unit

// TODO: Unused
typealias SaasOngoingAuthorizationCompletion = (credentials: CredentialsPayload?, error: Error?) -> Unit

typealias AvailableServicesCompletion = (ServicesResponse?, error: Error?) -> Unit

typealias UserDeleteCompletion = (isLibraryDeleted: Boolean?, error: Error?) -> Unit

// TODO: Unused
typealias GetSessionCompletion = (isSessionUpdated: Boolean?, error: Error?) -> Unit

typealias GetAuthorizationDoneCompletion = (response: AuthorizationResponse?, error: Error?) -> Unit