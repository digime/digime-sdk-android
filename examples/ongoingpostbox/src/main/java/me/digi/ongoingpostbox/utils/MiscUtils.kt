package me.digi.ongoingpostbox.utils

import android.app.Activity
import android.content.Context
import android.net.Uri
import java.io.IOException

fun getFileContent(activity: Activity, fileName: String): ByteArray {
    return try {
        val stream = activity.assets.open(fileName)
        val size = stream.available()
        val buffer = ByteArray(size)
        stream.read(buffer)
        stream.close()
        buffer
    } catch (ex: IOException) {
        ex.printStackTrace()
        return ByteArray(2)
    }
}

@Throws(IOException::class)
fun readBytes(context: Context, uri: Uri): ByteArray? =
    context.contentResolver.openInputStream(uri)?.buffered()?.use { it.readBytes() }