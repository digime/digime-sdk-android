package me.digi.examples.ongoing.utils

import android.content.Context
import java.io.File

class FileUtils(private val context: Context) {
    fun storeBytes(bytes: ByteArray, name: String): File {

        val absolutePath = context.filesDir.absolutePath + "/cachedsongs/" + name

        val dirs = absolutePath.split("/").dropLast(1).joinToString("/")

        val dirHandle = File(dirs)
        dirHandle.mkdirs()

        val fileHandle = File(absolutePath)
        if (!fileHandle.exists()) {
            fileHandle.createNewFile()
        }
        fileHandle.writeBytes(bytes)
        return fileHandle
    }

    fun listFilesInCache(): List<File> {
        return File(context.filesDir.absolutePath + "/cachedsongs/").listFiles()?.toList() ?: emptyList()
    }

    fun nukeCache() {
        File(context.filesDir.absolutePath + "/cachedsongs/")
            .takeIf { it.exists() }
            .also { it?.deleteRecursively() }
    }
}