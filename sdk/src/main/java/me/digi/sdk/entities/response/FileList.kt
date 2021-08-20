package me.digi.sdk.entities.response

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.JsonAdapter
import com.google.gson.reflect.TypeToken
import me.digi.sdk.entities.FileListAccount
import me.digi.sdk.entities.FileListItem
import java.lang.reflect.Type

@JsonAdapter(FileListDeserializer::class)
class FileList(
    val fileList: List<FileListItem>,
    val syncStatus: SyncStatus,
    val accounts: List<FileListAccount>?
) {

    open class SyncStatus(val rawValue: String) {
        class PENDING : SyncStatus("pending")
        class RUNNING : SyncStatus("running")
        class COMPLETED : SyncStatus("completed")
        class PARTIAL : SyncStatus("partial")

        override fun equals(other: Any?): Boolean {
            return if (other is SyncStatus) {
                other.rawValue == this.rawValue
            } else false
        }
    }
}

private class FileListDeserializer : JsonDeserializer<FileList> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): FileList {

        (json as? JsonObject)?.let {

            val fileListItemsType = object : TypeToken<List<FileListItem>>() {}.type
            val fileListItems = try {
                context?.deserialize<List<FileListItem>>(it.get("fileList"), fileListItemsType)
                    ?: emptyList()
            } catch (e: Throwable) {
                emptyList<FileListItem>()
            }

            val status = it.getAsJsonObject("status")
            val syncStateRaw = status.getAsJsonPrimitive("state").asString
            val syncState = FileList.SyncStatus(syncStateRaw)

            val accountsRaw = status.getAsJsonObject("details")
            val accountIds = accountsRaw?.keySet()
            val accounts = accountIds?.map { accountId ->
                val accountRaw = accountsRaw.getAsJsonObject(accountId)
                val accSyncStateRaw = accountRaw.getAsJsonPrimitive("state").asString
                val accSyncState = FileList.SyncStatus(accSyncStateRaw)
                val accError = context?.deserialize<Map<String, Any>?>(
                    accountRaw.getAsJsonObject("error"),
                    Map::class.java
                )
                FileListAccount(accountId, accSyncState, accError)
            }

            return FileList(fileListItems, syncState, accounts)

        } ?: run { throw IllegalArgumentException() }
    }
}