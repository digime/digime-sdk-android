package me.digi.sdk.crypto

import android.content.Context
import android.util.Base64
import androidx.test.core.app.ApplicationProvider
import me.digi.sdk.utilities.crypto.ByteTransformer
import me.digi.sdk.utilities.crypto.CryptoUtilities
import me.digi.sdk.utilities.crypto.KeyTransformer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.nio.charset.StandardCharsets
import java.security.Security

@RunWith(RobolectricTestRunner::class)
class CryptoUtilitiesSpec {

    @Test
    fun `given valid p12 file and pass, key is parsed`() {

        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val key = CryptoUtilities(ctx).privateKeyHexFrom("CA_RSA_PRIVATE_KEY.p12", "monkey periscope")

        // An exception will be thrown by DMECryptoUtilities if errors are encountered.
        assertNotEquals("", key)
    }

    @Test
    fun `given input string, hashed output should match sha512 hash of input`() {

        val input = "The quick brown fox jumps over the lazy dog."
        val preHashedInput = "91ea1245f20d46ae9a037a989f54f1f790f0a47607eeb8a14d12890cea77a1bbc6c7ed9cf205e67b7f2b8fd4c7dfd3a7a8617e45f3c463d481c7e586c39ac1ed"
        val output = CryptoUtilities.hashData(input.toByteArray(StandardCharsets.UTF_8))
        assertEquals(preHashedInput.toUpperCase(), output.toUpperCase())
    }

    @Test
    fun `given a valid key, correct output is decrypted via RSA from input`() {

        Security.addProvider(BouncyCastleProvider())

        val expectedOutput = "The quick brown fox jumps over the lazy dog."

        val encryptedDataBase64 = """
            oxu7OKiTC+yREI/4xWwZUrSonU73v1fbnWPGs6qbS1Ao/MAn+uj+9V6t/aliHwXQ
            UX7muJN3sTEgC4AZdiOf7Nw73Sq7Fhxq7aFFa1VLhvrTkeLn6wabuxwWK0M5dTFo
            rlm0dqbmFGigcXlcWUe72Rb0h6oJ/zkw3eJ9NsL5anv1frciyc9Dl5ilHY9hhbRs
            at8LK4BkxVfeP0hvzKLMMlITFBOFsJmYGEbvmy6J2bJpGoIp8U7vTtNhVL52A7sP
            1t0dLGKFnD+7uoES+2XgqnzfDlaNdHtkNiK6mbMBajDZtWOZB9rQjP/T1XGJvvou
            wXD7l3S+fu1njOul71wSPA==
            """.trimIndent()

        val encryptedData = Base64.decode(encryptedDataBase64, Base64.DEFAULT)

        val privateKeyBase64 = """
            MIIEpAIBAAKCAQEApJgu7VdGVsxleKzsgBcfR6qV8LTriYVQHNZg+WOWibTYkh6w
            fW2+Y85rVydzGqtBfzdTeBPbCo5hqIsUgPZRi0pZo9uDR5YnzEv8Yvv1wFg0jm+Y
            7dqbUcTtpUeGN0FexTQidNp6A9cifuMwoLbL5FD6QhAnGhEFxy3DZGg9i6447zWh
            +wARzQ2a8oiYq+5kaKC63XdmVC5aY+dbEtDx2Gjf1d1bJ1WDQ0hvJdB7iAIrKEOl
            27TKX6w2YjATqAFPZPoWbeq6UAfgppcFAGDz+Mz7IvbOfpcbaIO6RVU5MPGXexkL
            sgWImO1vkrMGGXfaz+x0RwqRMGDxChQMgWDEVwIDAQABAoIBACdiiwgb9Cbc7gL8
            TfMwlTmO5iYdBKd5kPv1iGrkm6cD8Ta1xUGeOwqi4CiQtghRpnCMipqXFbjm7RNp
            hFR/nG+aMOz2n+lHUuqd3BzLcbi+4oSay5tOzg+48ay0+rbKG6VQnlYb2UqF9ikq
            r/4k/5D4l4mrV8/y9tD8cF7cWq3Ldk1EiHgeK8Eb+reevV5IGb7JjntXfnbnGvT5
            LhBDHtNwqwtp6fC0yELy4gtP5e6pXl0TBNP2u52jyFHybkGSsufIqmUuvCnKXkCl
            48c4DpPH7OiEOvTd4dLJz047H0ny8LiuGHyLrJnmOpuW7LKhtKc4uA57WkBqvXAU
            BfEpMTECgYEA2OBffjhlESqaL/8Sdvss8sp1tvSGCSpPvV90Uz7u2La5wwyxawl0
            sLeLXRa3cS4KLiTNvzd4/XrqtxdxnzuijGK68ZNOUnjZROwRDeB8xPDcuLEDgtVh
            WrOmFH1HoVMKc3zq0X2GkwEJ/ZD/vpcxXXg0wZs9egCh6QWqmcAkb9MCgYEAwklc
            wWDgez4wwGZUQjEsI9Vr3i+QEshrbio4zPiHUQRwWnVQSWa9HxRKSyBFprKnjq+H
            wvC0WNIXKykBYbcnN9nuqVh8lWjrBj7QdpFZVM71PltWuFTMg5wGpy9ILFYlIUGv
            Zma1Eo9SJkOgReI5T6bWCmELeHvmYCfo+Xo9Cu0CgYEAzvILLR4TErZcOsBiljZH
            RcX7XYEnBUfinwmernC26QQzbfOGuGRlfNsnmMQ8dzs/a5ii23vLb9UCc4NjYFY/
            XUY4rJurePboG+RGwb8cT7CmbXY7q3SBWVNjftmsqDvdFSZ1y0DEUTIEnnjmEK+k
            41P4w2Wm2+wOk5RcninCmxECgYEAgX8ohfQua7beA7+w9ZWU2CWOij+lhf90K6+U
            wHn+p2+P+5sp8mK6N5bslfpismNt71rr4HFTo8gUjT39n8XWLBHkU5eZInUWAcmo
            ZP8oTbDMIc37lU1gK3C5toF7V6HriakgYd6fXkmM9dgpYasRjBelnrFkVeAvg3PW
            g0KIoVkCgYANe6lAM/40MLlloI81Ji0Cde8u/ZKCV/gkT5x1M973oYDpMcbAa3v/
            ixodp6qaEsw70dOgccvAsgDmtQZZNYdFm1PIIAjay7P8eZ4pLtIDKwkfwHjU2CCx
            FyfqE2TJZCjFrUPfA/HeRCrMGei2rVxoJ9OmAktwLIzIL4yDcjN7Mg==
            """.trimIndent()

        val privateKeyBytes = Base64.decode(privateKeyBase64, Base64.DEFAULT)
        val privateKey = KeyTransformer.javaPrivateKeyFromBytes(privateKeyBytes)

        val decryptedBytes = CryptoUtilities.decryptRSA(encryptedData, privateKey)
        val decryptedString = String(decryptedBytes, StandardCharsets.UTF_8)

        Security.removeProvider("SC")

        assertEquals(expectedOutput, decryptedString)
    }

    @Test
    fun `given a valid key, correct output is decrypted via AES256 from input`() {

        Security.addProvider(BouncyCastleProvider())

        val inputBase64 = "FDmpYpc+YSNUXck1qrJUSbhV12sNWL676mOoVt0Pdo3SSr32DH/9iV3d7lHWorhu"
        val keyHex = "FF867603AEE1AB7D380415EFB3A822148066AAE7975A465510A3DF4EB2CDCAE7"
        val ivHex = "CAA6DA6EFFC5EC24B49162A04E50F01E"

        val inputBytes = Base64.decode(inputBase64, Base64.DEFAULT)
        val keyBytes = ByteTransformer.bytesFromHexString(keyHex)
        val ivBytes = ByteTransformer.bytesFromHexString(ivHex)

        val decryptedBytes = CryptoUtilities.decryptAES(inputBytes, keyBytes, ivBytes)

        val expectedOutput = "The quick brown fox jumps over the lazy dog."
        val actualOutput = String(decryptedBytes, StandardCharsets.UTF_8)

        Security.removeProvider("SC")

        assertEquals(expectedOutput, actualOutput)
    }
}