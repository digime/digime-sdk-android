package me.digi.sdk.crypto

import me.digi.sdk.utilities.crypto.ByteTransformer
import org.junit.Assert.assertEquals
import org.junit.Test

class ByteTransformerSpec {

    @Test
    fun `given input ensure output matches when transformed to bytes and back`() {

        val hexInput = "16ea4f28bc99c4a1"
        val bytesIntermediate = ByteTransformer.bytesFromHexString(hexInput)
        val output = ByteTransformer.hexStringFromBytes(bytesIntermediate)

        assertEquals(hexInput, output)
    }
}