package me.digi.sdk.utilities

import android.os.Bundle
import android.text.format.Time
import me.digi.sdk.entities.FileListAccount

fun Bundle.toMap() = this.keySet().map { it to this.get(it) }.toMap()

fun Long.isValid(): Boolean {
    return this > (System.currentTimeMillis() / 1000) - 5 * Time.MINUTE
}

fun List<FileListAccount>.hasError(): Boolean {
    this.forEach {
        if (it.error != null)
            return true
    }

    return false
}

fun List<FileListAccount>.errorAccounts(): List<String> {
    val accountIds = mutableListOf<String>()
    this.forEach {
        if (it.error != null)
            accountIds.add(it.identifier)
    }

    return accountIds
}