package me.digi.sdk.utilities

import android.os.Bundle
import android.text.format.Time
import me.digi.sdk.APIError
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

fun List<FileListAccount>.errorAccounts(): List<FileListAccount> {
    val errorAccounts = mutableListOf<FileListAccount>()
    this.forEach {
        if (it.error != null)
            errorAccounts.add(it)
    }

    return errorAccounts
}

fun List<FileListAccount>.parseError(): APIError {
    return when(this.first().error?.get("statuscode")){
        511.0 -> {
            APIError.REAUTHREQUIRED()
        }
        401 -> {
            APIError.INVALID_REFRESH_TOKEN()
        }
        else ->
            APIError.GENERIC()
    }
}