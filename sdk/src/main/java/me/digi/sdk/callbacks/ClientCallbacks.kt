package me.digi.sdk.callbacks

import me.digi.sdk.Error
import me.digi.sdk.entities.GetTokenExchangeDone
import me.digi.sdk.entities.response.*
import me.digi.sdk.entities.service.ServicesResponse

typealias AuthorizationCompletion = (consentAuthResponse: ConsentAuthResponse?, error: Error?) -> Unit

typealias ServiceOnboardingCompletion = (onboardAuthResponse: OnboardAuthResponse?, error: Error?) -> Unit

typealias FileListCompletion = (fileList: FileList?, error: Error?) -> Unit

typealias FileContentCompletion = (fileItem: FileItem?, error: Error?) -> Unit

typealias FileContentBytesCompletion = (fileItem: FileItemBytes?, error: Error?) -> Unit

typealias AccountsCompletion = (accounts: ReadAccountsResponse?, error: Error?) -> Unit

typealias IncrementalFileListUpdate = (fileList: FileList, updatedFileIds: List<String>) -> Unit

typealias AvailableServicesCompletion = (ServicesResponse?, error: Error?) -> Unit

typealias UserDeleteCompletion = (isLibraryDeleted: Boolean?, error: Error?) -> Unit

typealias GetAuthorizationDoneCompletion = (response: AuthorizationResponse?, error: Error?) -> Unit

typealias GetOnboardDoneCompletion = (response: OnboardResponse?, error: Error?) -> Unit

typealias OngoingWriteCompletion = (isDataPushed: Boolean?, error: Error?) -> Unit

typealias GetSessionCompletion = (isSessionUpdated: Boolean?, error: Error?) -> Unit

typealias RefreshCompletion = (tokenExchangeResponse: GetTokenExchangeDone?, error: Error?) -> Unit