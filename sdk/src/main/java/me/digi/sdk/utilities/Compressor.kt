package me.digi.sdk.utilities

import me.digi.sdk.SDKError
import java.util.zip.GZIPInputStream

object Compressor {

    const val COMPRESSION_NONE = ""
    const val COMPRESSION_GZIP = "gzip"
    const val COMPRESSION_BROTLI = "brotli"

    fun decompressData(data: ByteArray, compression: String) = when (compression) {

        COMPRESSION_NONE -> data

        COMPRESSION_GZIP -> {

            val gzipInputStream = GZIPInputStream(data.inputStream())
//            var bytesRead: Int
//
//            var intermediateData = ByteArray(32)
//            val sb = StringBuilder()
//
//            while(true) {
//                bytesRead = gzipInputStream.read(intermediateData)
//
//                if (bytesRead == -1) {
//                    break
//                }
//
//                sb.append(String(intermediateData, 0, bytesRead))
//            }
//
//            sb.toString()

            gzipInputStream.readBytes()
        }

        else -> throw SDKError.InvalidData()
    }
}