package me.digi.ongoingpostbox.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import me.digi.ongoingpostbox.R
import java.io.IOException
import java.util.*

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

fun getMimeType(context: Context, uri: Uri): String? {
    var mimeType: String? = null
    mimeType = if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
        val cr: ContentResolver = context.contentResolver
        cr.getType(uri)
    } else {
        val fileExtension: String = MimeTypeMap.getFileExtensionFromUrl(
            uri
                .toString()
        )
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            fileExtension.toLowerCase(Locale.getDefault())
        )
    }
    return mimeType
}

fun Fragment.replaceFragment(fragmentManager: FragmentManager) {
    fragmentManager
        .beginTransaction()
        .replace(R.id.navigation_fragment_holder, this)
        .commit()
}