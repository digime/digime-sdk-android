package me.digi.sdk.entities

import me.digi.sdk.entities.response.FileList

data class FileListAccount(
    val identifier: String,
    val syncStatus: FileList.SyncStatus,
    val error: Map<String, Any>?
)