package me.digi.sdk.entities

import me.digi.sdk.entities.response.DMEFileList

data class FileListAccount(
    val identifier: String,
    val syncStatus: DMEFileList.SyncStatus,
    val error: Map<String, Any>?
)