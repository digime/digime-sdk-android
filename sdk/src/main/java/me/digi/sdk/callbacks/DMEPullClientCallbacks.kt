package me.digi.sdk.callbacks

import me.digi.sdk.DMEError
import me.digi.sdk.entities.*
import me.digi.sdk.saas.serviceentities.Service
import me.digi.sdk.saas.serviceentities.ServicesResponse

typealias DMEAuthorizationCompletion = (session: DMESession?, error: DMEError?) -> Unit

typealias AuthorizationCompletion = (authSession: AuthSession?, error: DMEError?) -> Unit

typealias OnboardingCompletion = (error: DMEError?) -> Unit

typealias DMEOngoingAuthorizationCompletion = (session: DMESession?, credentials: DMEOAuthToken?, error: DMEError?) -> Unit

typealias DMEFileListCompletion = (fileList: DMEFileList?, error: DMEError?) -> Unit

typealias DMEFileContentCompletion = (file: DMEFile?, error: DMEError?) -> Unit

typealias DMEAccountsCompletion = (accounts: List<DMEAccount>?, error: DMEError?) -> Unit

typealias DMEIncrementalFileListUpdate = (fileList: DMEFileList, updatedFileIds: List<String>) -> Unit

typealias DMEServicesForContractCompletion = (services: List<Service>?, error: DMEError?) -> Unit

typealias DMESaasOngoingAuthorizationCompletion = (exchangeTokens: DMETokenExchange?, error: DMEError?) -> Unit

typealias DMEServicesForContract = (ServicesResponse?, error: DMEError?) -> Unit