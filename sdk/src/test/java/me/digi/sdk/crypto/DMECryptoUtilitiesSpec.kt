package me.digi.sdk.crypto

import me.digi.sdk.utilities.crypto.DMECryptoUtilities
import org.junit.Test

class DMECryptoUtilitiesSpec {

    @Test
    fun `given valid p12 file and pass, key is imported`() {
        ClassLoader.getSystemClassLoader().getResource("src/test/assets/TestPrivateKey.p12")
        val p12Pass = "digime"
        val key = DMECryptoUtilities.privateKeyHexFrom("src/test/assets/TestPrivateKey.p12", p12Pass)
        print(key)
        assert(key != "")
    }
}