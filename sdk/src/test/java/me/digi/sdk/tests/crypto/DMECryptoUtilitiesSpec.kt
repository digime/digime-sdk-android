package me.digi.sdk.crypto

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DMECryptoUtilitiesSpec {

    @Test
    fun `given valid p12 file and pass, key is parsed`() {

        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val key = DMECryptoUtilities(ctx).privateKeyHexFrom("CA_RSA_PRIVATE_KEY.p12", "monkey periscope")

        // An exception will be thrown by DMECryptoUtilities if errors are encountered.
        assertNotEquals("", key)
    }
}