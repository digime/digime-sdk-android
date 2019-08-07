package me.digi.sdk.crypto

import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@Config(assets="")
@RunWith(RobolectricTestRunner::class)
class DMECryptoUtilitiesSpec {

    @Test
    fun `given valid p12 file and pass, key is imported`() {
        val p12Pass = "monkey periscope"

        val ctx = RuntimeEnvironment.systemContext

        val key = DMECryptoUtilities(ctx).privateKeyHexFrom("src/test/assets/CA_RSA_PRIVATE_KEY.p12", p12Pass)
        print(key)
        assert(key != "")
    }
}