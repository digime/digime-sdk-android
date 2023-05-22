package me.digi.sdk.callbacks

import me.digi.sdk.Error
import me.digi.sdk.entities.TokenExchangeResponse
import me.digi.sdk.entities.payload.CredentialsPayload
import me.digi.sdk.entities.response.*
import me.digi.sdk.entities.service.ServicesResponse


typealias AuthorizeAccessCallback = (response: AuthorizationResponse?, error: Error?) -> Unit

typealias DeleteUserCallback = (response: DeleteUserResponse?, error: Error?) -> Unit

typealias GetAccountsCallback = (accounts: GetAccountsResponse?, error: Error?) -> Unit

typealias WriteDataCallback = (response: WriteDataResponse?, error: Error?) -> Unit

typealias GetSessionCallback = (isSessionUpdated: Boolean?, error: Error?) -> Unit

typealias AvailableServicesCallback = (ServicesResponse?, error: Error?) -> Unit

typealias FileContentCallback = (fileItem: FileItem?, error: Error?) -> Unit

typealias FileListCallback = (fileList: FileList?, error: Error?) -> Unit

typealias RefreshTokenCallback = (tokenExchangeResponse: TokenExchangeResponse?, error: Error?) -> Unit

typealias FileContentBytesCallback = (fileItem: FileItemBytes?, error: Error?) -> Unit

typealias IncrementalFileListUpdate = (fileList: FileList, updatedFileIds: List<String>) -> Unit

typealias CredentialsCallback = (credentials: CredentialsPayload?, error: Error?) -> Unit

typealias PortabilityReportCallback = (portabilityReportResponse: PortabilityReportResponse?, error: Error?) -> Unit
