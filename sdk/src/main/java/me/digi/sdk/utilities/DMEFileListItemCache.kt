package me.digi.sdk.utilities

import me.digi.sdk.entities.FileListItem

internal class DMEFileListItemCache {

    private var cachedFileListItems = emptyList<FileListItem>()

    fun updateCacheWithItemsAndDeduceChanges(items: List<FileListItem>): List<String> {
        val cachedItemsMappedByID = cachedFileListItems.map { it.fileId to it.updatedDate }.toMap()
        val newItemsMappedByID = items.map { it.fileId to it.updatedDate }.toMap()

        val mergeOutput = cachedItemsMappedByID.merged(newItemsMappedByID) { left, right ->
            if (right > left) right else left
        }

        val mergedItemsMappedByID = mergeOutput.first
        val changedKeys = mergeOutput.second

        cachedFileListItems = mergedItemsMappedByID
            .map { it }
            .fold(listOf<FileListItem>()) { cumulative, mapItem ->
                val new = cumulative.toMutableList()
                new.add(FileListItem(mapItem.key, mapItem.value))
                new
            }

        return changedKeys
    }
}

private fun <K, V> Map<K, V>.merged(other: Map<K, V>, conflictResolver: (left: V, right: V) -> V): Pair<Map<K, V>, List<K>> {

    val merged = this.toMutableMap()
    val changedKeys = mutableListOf<K>()

    for (entry in other) {
        if (!merged.containsKey(entry.key)) {
            merged[entry.key] = entry.value
            changedKeys.add(entry.key)
        }
        else {
            val resolvedValue = conflictResolver.invoke(merged[entry.key]!!, other[entry.key]!!)
            if (resolvedValue == other[entry.key]!! && resolvedValue != merged[entry.key]!!)
                changedKeys.add(entry.key)
            merged[entry.key] = resolvedValue
        }
    }

    return Pair(merged, changedKeys)
}