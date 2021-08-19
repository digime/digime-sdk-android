package me.digi.sdk.tests.utilities.crypto

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import me.digi.sdk.utilities.crypto.CryptoUtilities
import me.digi.sdk.utilities.crypto.KeyTransformer
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class KeyTransformerSpec {

    @Test
    fun `given key hex, transform to java priv key and back, ensure matches`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val hexInput = CryptoUtilities(ctx).privateKeyHexFrom("CA_RSA_PRIVATE_KEY.p12", "monkey periscope")

        val pkIntermediate = KeyTransformer.javaPrivateKeyFromHex(hexInput)
        val hexOutput = KeyTransformer.hexFromJavaPrivateKey(pkIntermediate)

        assertEquals(hexInput, hexOutput)
    }
}