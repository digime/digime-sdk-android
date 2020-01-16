package me.digi.sdk.utilities.jwt

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.MGF1ParameterSpec
import java.security.spec.PSSParameterSpec
import kotlin.reflect.KProperty

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
internal annotation class JwtClaim

internal abstract class JsonWebToken(tokenised: String? = null) {

    companion object {
        @JvmStatic
        protected val BASE64_FLAGS = Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
    }

    private val gsonAgent: Gson by lazy { GsonBuilder().create() }
    private val signer: Signature by lazy {
        val signer = Signature.getInstance("SHA512withRSA/PSS", "SC")
        val parameter = PSSParameterSpec(MGF1ParameterSpec.SHA512.digestAlgorithm, "MGF1", MGF1ParameterSpec.SHA512, 64, 1)
        signer.setParameter(parameter)
        signer
    }

    private lateinit var header: Map<String, Any>
    private lateinit var payload: Map<String, Any>
    private lateinit var signature: ByteArray

    init {
        if (tokenised != null) {
            // TODO: Decode here.
            header = emptyMap()
            payload = emptyMap()
        }
    }

    fun encodedHeader(): String {

        if (!::header.isInitialized) {
            // Header defaults:
            header = mapOf(
                "typ" to "JWT",
                "alg" to "PS512"
            )
        }

        val bytes = gsonAgent.toJsonTree(header).toString().toByteArray()
        return Base64.encodeToString(bytes, BASE64_FLAGS)
    }

    fun encodedPayload(): String {

        if (!::payload.isInitialized) {
            // Parse claims for body.
            val claimFields = this::class.members.mapNotNull { it as? KProperty }.filter { it.annotations.any { it is JwtClaim }}
            payload = claimFields.mapNotNull {
                val key = it.name.replace("(.)([A-Z]+)".toRegex(), "$1_$2").toLowerCase()
                val value = it.getter.call(this)

                value?.let { Pair(key, value) }

            }.toMap()
        }

        val bytes = gsonAgent.toJsonTree(payload).toString().toByteArray()
        return Base64.encodeToString(bytes, BASE64_FLAGS)
    }

    fun encodedSignature(): String {

        if (!::signature.isInitialized) {
            throw IllegalStateException("You have not yet signed this JWT.")
        }

        return Base64.encodeToString(signature, BASE64_FLAGS)
    }

    fun sign(key: PrivateKey): JsonWebToken {

        val signableBytes = "${encodedHeader()}.${encodedPayload()}".toByteArray()

        signer.initSign(key)
        signer.update(signableBytes)

        signature = signer.sign()

        return this
    }

    open fun tokenize() = listOf(encodedHeader(), encodedPayload(), encodedSignature()).joinToString(".")
}