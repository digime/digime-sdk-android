package me.digi.sdk.entities

data class DMEFileListAccount (

    val identifier: String,

    val syncStatus: DMEFileList.SyncStatus,

    val error: Map<String, Any>?

)