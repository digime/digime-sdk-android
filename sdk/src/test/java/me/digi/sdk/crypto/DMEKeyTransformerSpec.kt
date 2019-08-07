package me.digi.sdk.crypto

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import me.digi.sdk.utilities.crypto.DMEKeyTransformer
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DMEKeyTransformerSpec {

    @Test
    fun `given key hex, transform to java priv key and back, ensure matches`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val hexInput = DMECryptoUtilities(ctx).privateKeyHexFrom("CA_RSA_PRIVATE_KEY.p12", "monkey periscope")

        val pkIntermediate = DMEKeyTransformer.javaPrivateKeyFromHex(hexInput)
        val hexOutput = DMEKeyTransformer.hexFromJavaPrivateKey(pkIntermediate)

        assertEquals(hexInput, hexOutput)
    }
}