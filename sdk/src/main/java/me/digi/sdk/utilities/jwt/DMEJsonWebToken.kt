package me.digi.sdk.utilities.jwt

import android.util.Base64
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.lang.reflect.Type
import java.security.PrivateKey
import java.security.Security
import java.security.Signature
import java.security.spec.MGF1ParameterSpec
import java.security.spec.PSSParameterSpec
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
internal annotation class JwtClaim

internal open class JsonWebToken(tokenised: String? = null) {

    companion object {
        @JvmStatic
        protected val BASE64_FLAGS = Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
    }

    private val gsonAgent: Gson by lazy { GsonBuilder().create() }
    private val signer: Signature by lazy {
        Security.addProvider(BouncyCastleProvider())
        val signer = Signature.getInstance("SHA512withRSA/PSS", "SC")
        val parameter = PSSParameterSpec(
            MGF1ParameterSpec.SHA512.digestAlgorithm,
            "MGF1",
            MGF1ParameterSpec.SHA512,
            64,
            1
        )
        signer.setParameter(parameter)
        signer
    }

    lateinit var header: Map<String, Any>
    lateinit var payload: Map<String, Any>
    lateinit var signature: ByteArray

    init {
        if (tokenised != null) {
            val jwtComponents = tokenised.split(".")

            val encodedHeader = jwtComponents[0]
            val encodedPayload = jwtComponents[1]
            val encodedSignature = jwtComponents[2]

            signature = Base64.decode(encodedSignature, BASE64_FLAGS)
            header = gsonAgent.fromJson<Map<String, Any>>(Base64.decode(encodedHeader, BASE64_FLAGS)
                .toString(
                    Charsets.UTF_8
                ), object : TypeToken<Map<String, Any>>() {}.type
            )
            payload = gsonAgent.fromJson<Map<String, Any>>(Base64.decode(
                encodedPayload,
                BASE64_FLAGS
            ).toString(Charsets.UTF_8), object : TypeToken<Map<String, Any>>() {}.type
            )

            // Parse claims for body.
            val claimFields = this::class.members.mapNotNull { it as? KMutableProperty }.filter { it.annotations.any { it is JwtClaim }}
            claimFields.forEach { field ->
                val key = field.name.replace("(.)([A-Z]+)".toRegex(), "$1_$2").toLowerCase()
                val value = payload[key]
                field.isAccessible = true
                field.setter.call(this, value)
            }
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

    open fun tokenize() = listOf(encodedHeader(), encodedPayload(), encodedSignature()).joinToString(
        "."
    )

    open inner class Adapter<T : JsonWebToken>: JsonSerializer<T>, JsonDeserializer<T> {
        override fun serialize(src: T, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            return JsonPrimitive(src.tokenize())
        }

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): T {
            val source = json ?: throw IllegalArgumentException()

            val tokenised = when (source) {
                is JsonPrimitive -> source.asString
                is JsonObject -> source["token"].asString
                else -> throw IllegalArgumentException()
            }

            @Suppress("UNCHECKED_CAST")
            return (typeOfT as Class<T>).kotlin.primaryConstructor!!.call(tokenised)
        }
    }
}