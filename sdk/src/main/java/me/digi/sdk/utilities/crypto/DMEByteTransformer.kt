package me.digi.sdk.utilities.crypto

object DMEByteTransformer {

    fun hexStringFromBytes(bytes: ByteArray): String {
        return bytes.fold("") { cum, inc ->
            cum + String.format("%02x", inc)
        }
    }

    fun bytesFromHexString(hex: String): ByteArray {

        var strLen = hex.length

        val mutableBytes = ByteArray(strLen / 2)

        for (i in 0 until strLen - 1 step 2) {
            mutableBytes[i / 2] = ((Character.digit(hex.toCharArray()[i], 16) shl 4)
                    + Character.digit(hex.toCharArray()[i + 1], 16)).toByte()
        }

        return mutableBytes
    }
}