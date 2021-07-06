package me.digi.saas.utils

import android.app.Activity
import java.io.IOException

fun Activity.getFileContent(fileName: String): ByteArray {
    return try {
        val stream = assets.open(fileName)
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