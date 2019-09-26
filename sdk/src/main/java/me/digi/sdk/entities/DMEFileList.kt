package me.digi.sdk.entities

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.JsonAdapter
import com.google.gson.reflect.TypeToken
import me.digi.sdk.utilities.DMELog
import java.lang.reflect.Type

@JsonAdapter(DMEFileListDeserializer::class)
internal class DMEFileList (

    val fileList: List<DMEFileListItem>,
    val state: SyncState

)
{

    open class SyncState(val rawValue: String) {
        class PENDING: SyncState("pending")
        class RUNNING: SyncState("running")
        class COMPLETED: SyncState("completed")
        class PARTIAL: SyncState("partial")

        override fun equals(other: Any?): Boolean {
            return if (other is SyncState) {
                other.rawValue == this.rawValue
            }
            else false
        }
    }
}

private class DMEFileListDeserializer: JsonDeserializer<DMEFileList> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): DMEFileList {

        (json as? JsonObject)?.let {

            val fileListItemsType = object: TypeToken<List<DMEFileListItem>>() {}.type
            val fileListItems = try {
                context?.deserialize<List<DMEFileListItem>>(it.get("fileList"), fileListItemsType) ?: emptyList()
            }
            catch(e: Throwable) {
                emptyList<DMEFileListItem>()
            }

            val status = it.getAsJsonObject("status")
            val syncStateRaw = status.getAsJsonPrimitive("state").asString
            val syncState = DMEFileList.SyncState(syncStateRaw)

            return DMEFileList(fileListItems, syncState)

        } ?: run { throw IllegalArgumentException() }
    }
}