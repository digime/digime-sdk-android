package me.digi.barebonesapp.util

import android.content.res.AssetManager
import android.util.Base64
import org.bouncycastle.asn1.ASN1InputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec

class CryptoUtil {

    inner class ResultsData(base64EncryptedKey: String, base64encodedMetadata: String, postboxId: String, iv: String) {
        internal var base64EncryptedKey = ""
        internal var base64encodedMetadata = ""
        internal var postboxId = ""
        internal var iv = ""

        init {
            this.base64EncryptedKey = base64EncryptedKey
            this.base64encodedMetadata = base64encodedMetadata
            this.postboxId = postboxId
            this.iv = iv
        }
    }

//    fun encryptData(publicKey: String, postboxId: String, filesDir: File, assets: AssetManager, fileName: String): ResultsData {
////        val key = CryptoUtils.pipeGenerateSecureRandom(32)
////        val rsa = toPublicKey(publicKey)
////        val encryptedKey = CryptoUtils.encryptRSA(key, rsa)
////        val base64EncryptedKey = org.bouncycastle.util.encoders.Base64.toBase64String(encryptedKey)
////
////        val iv = CryptoUtils.pipeGenerateSecureRandom(16)
////
////        val dataContent = getFileContent(fileName, assets)
////        val encryptedData = CryptoUtils.encryptAES(dataContent, key, iv)
////        saveFile(encryptedData, filesDir.absolutePath + "/file.json")
////
////        val metadataContent = getFileContent("metadata.json", assets)
////        val encryptedmetaData = CryptoUtils.encryptAES(metadataContent, key, iv)
////        val base64encodedMetadata = org.bouncycastle.util.encoders.Base64.toBase64String(encryptedmetaData)
//
//        return ResultsData(base64EncryptedKey, base64encodedMetadata, postboxId, iv.toHexString())
//    }

    private fun getFileContent(fileName: String, assets: AssetManager):ByteArray {
        return try {
            val stream = assets.open(fileName)
            val size = stream.available()
            val buffer = ByteArray(size)
            stream.read(buffer)
            stream.close()
//            String(buffer, Charsets.UTF_8)
            buffer
        } catch (ex: IOException) {
            ex.printStackTrace()
//            return ""
            return ByteArray(2)
        }
    }

    @Throws(Exception::class)
    fun saveFile(fileData: ByteArray, path: String) {
        val file = File(path)
        val bos = BufferedOutputStream(FileOutputStream(file, false))
        bos.write(fileData)
        bos.flush()
        bos.close()
    }

    private fun toPublicKey(public: String): PublicKey {
        val publicKeyContent = public.replace("\n", "").replace("-----BEGIN RSA PUBLIC KEY-----", "").replace("-----END RSA PUBLIC KEY-----", "")

        val rsaPublicKeyBytes = Base64.decode(publicKeyContent, Base64.DEFAULT)
        val rsaPublicKey = org.bouncycastle.asn1.pkcs.RSAPublicKey.getInstance(ASN1InputStream(rsaPublicKeyBytes).readObject())
        return KeyFactory.getInstance("RSA").generatePublic(RSAPublicKeySpec(rsaPublicKey.modulus, rsaPublicKey.publicExponent))
    }

    private fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
}