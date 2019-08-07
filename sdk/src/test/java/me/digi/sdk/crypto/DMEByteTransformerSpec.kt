package me.digi.sdk.crypto

import me.digi.sdk.utilities.crypto.DMEByteTransformer
import org.junit.Test
import org.junit.Assert.*

class DMEByteTransformerSpec {

    @Test
    fun `given input ensure output matches when transformed to bytes and back`() {

        val hexInput = "16ea4f28bc99c4a1"
        val bytesIntermediate = DMEByteTransformer.bytesFromHexString(hexInput)
        val output = DMEByteTransformer.hexStringFromBytes(bytesIntermediate)

        assertEquals(hexInput, output)
    }
}