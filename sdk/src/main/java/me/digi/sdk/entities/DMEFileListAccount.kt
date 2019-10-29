package me.digi.sdk.entities

data class DMEFileListAccount (

    val identifier: String,

    val syncState: DMEFileList.SyncState,

    val error: Map<String, Any>?

)