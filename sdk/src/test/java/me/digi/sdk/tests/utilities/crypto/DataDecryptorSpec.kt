package me.digi.sdk.tests.utilities.crypto

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import me.digi.sdk.entities.response.FileItem
import me.digi.sdk.entities.response.Status
import me.digi.sdk.utilities.crypto.DataDecryptor
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.security.Security

@RunWith(RobolectricTestRunner::class)
class DataDecryptorSpec {

    @Test
    fun `given encrypted JFS, dsk, iv and content should be deduced`() {

        Security.addProvider(BouncyCastleProvider())

        val encryptedDataBase64 = "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"
        val encryptedBytes = Base64.decode(encryptedDataBase64, Base64.DEFAULT)

        val privateKeyHex = "308204bd020100300d06092a864886f70d0101010500048204a7308204a302010002820101008e604708e03c86698173463a1a3eb5c999fd7506b0b6c4bbb3cfe267fc76a3fb87569f0b5d1554bcc5d8a621b9b9731179435035cb12ea080db7d7e9413d3c83942bb45fec5ccd1102eb7dcb1350e89f500cfb9aaf870dd200f5dfe052ccd68fa5548385245220d8902f524dc466b02049fb54e32e05d19e2cd5c1f6c7107c7f12f22904e31b01e4614483b92431feecbc4f194d501b806b9b927e6caadfbe2629467be8af40a706a1daff01a8366b355bb28c0a06421ffd1118f5b41496d7c5b05a88961e153978472f5b5f0d28e1258cf9a66a4afca8146754b75f8c618feb6d14eefec92eadf07ed87c4c02e28653347d98d234fab65a0bc56bec891b3c050203010001028201004924f3397f21f917d8f664dcd2c0e6d9b4affee03a071b9926eea4ed3149ae4339ab69b807d13ea060b045e14bae178ca194d06f626dcc27e90c4af33d9dfd29c8048ffd3bcc0d5a30fad4bc70a6ff6b614fda2f0500483c5c5bf4f124f8b13e48fcdb389a894af0975a8859058e8feb76b6954cc5471e02ba1bf33319dfc1652c19e77d8ee17f42407f95a47ce388b9861fb98b42d208be5553a3f5e2fefec13132dcc4919fa7a010b6206461be6ad125b062e97b863f7cb400016f5c1adb9632c469279421ec08a16e1d6026018fb904f77fcca7ece8cdf4d38113c88fdc2b32517fd397c1c11bec6032a95e58a65578a35d2bb47c11aafc190a09a7dfa8dd02818100e3b44013e9ad164c0416a258b2fa4751fee1f810b1fb4e5adc69cd6ebd6e403a2bb59b74d5d1a9855e480670ccdab8b6629ca6102de5991791feaa49e61c7b53c36e6c3256621145ebcf53dfa17a3a9a62b5be73be185aff9074e64bf2b4e7eb394fdc435ca4545a670ff6ccc4f9e639f00b141d7a0f415911b99ebae1ec2cb702818100a0118feca528fc8b8dd5b7adcf8da391a7adb74683bb647f06c0c9cc432097def5644e420f2096e03c632ccc2d85bd6edb8300c7704d8a5d2fa051270fdb77872ca45d9104b0d63273c6c6a563a7bbc68cdec83f7c0db23d0e853320d540034984b7e14f21a6cd924f7082ee2d0575c4fbc856fc6afe82954892fd537566d92302818022792da256247055bde0b75f7a694dd6ea21c7eeaf237eef5ea35e08c0012b14237df1353511f2fa5015d373cc6fe8bb241ed73d67d574e20ea6619da28af958eb239299e45e2083577a22169f59e3f96fac5853299a825c62c070055b8f6bf53ece0a68e063b8a99f24d99692d3cab946d0469157e36b5f52c1a9785dafe5ed0281803c89a7d72637b41bcb8eb2a327c637c0949c1c9eac9fa8c869f36bdeb6dc92d39e174b89ac52f4fce599d48d7c0202bcf4843d3d307a03046f48c0a87754d785e61a5bd0c6e7627b3e52453ab3cff2328b808e57844161d1b9b622ba1e2cd191728260d21eb609357699e4cad378272c52d55820ab0e8ab08efc189701e5dde50281810092166d635254a4e2b20d407c5b485aa31c3bcf0c070b15c5360a3cf4bd8f9ad7ed07790022b4be14ffc2d10711f04b55f2fe742ac2a608cb39bd7c657a453362ee5f5fa6077e3b0f777993c3a71d8d21adf659239c7c850153f2a759bebdc370312ed667855dfaaa4d8008756815672ff7772f162b67f4f6948cbe305dae09b9"

        val decrypted = DataDecryptor.dataFromEncryptedBytes(encryptedBytes, privateKeyHex)
        val file = FileItem(null, Status())
        val decryptedJSON = Gson().fromJson<List<Any>>(file.fileContentAsJSON(), object: TypeToken<List<Any>>(){}.type)

        Security.removeProvider("SC")

        assertTrue(decryptedJSON.count() > 0)
    }
}