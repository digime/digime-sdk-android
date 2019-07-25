package me.digi.sdk.callbacks

import me.digi.sdk.entities.DMEAccount
import me.digi.sdk.entities.DMEFile

typealias DMEFileContentCompletion = (file: DMEFile, error: Throwable) -> Unit

typealias DMEAccountsCompletion = (accounts: List<DMEAccount>, error: Throwable) -> Unit