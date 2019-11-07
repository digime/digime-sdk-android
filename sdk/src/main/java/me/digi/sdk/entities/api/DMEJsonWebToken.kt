package me.digi.sdk.entities.api

import android.util.Base64
import com.google.gson.*
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.lang.IllegalArgumentException
import java.lang.reflect.Type
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.MGF1ParameterSpec
import java.security.spec.PSSParameterSpec
import java.util.*

internal class DMEJsonWebToken (

    val header: Header,
    val payload: Payload,

    signature: String

) {

    var signature: String
        get() {
            if (field != "")
                return field

            // Annoyingly, to generate a signature we have to compile the whole JWT together.
            return Gson().toJson(this).split(".").last()
        }

    constructor(header: Header, payload: Payload): this(header, payload, "")

    init {
        this.signature = signature
    }

    internal class Adapter(val signingKey: PrivateKey): JsonSerializer<DMEJsonWebToken>, JsonDeserializer<DMEJsonWebToken> {
        override fun serialize(src: DMEJsonWebToken?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {

            if (src == null || context == null) {
                return JsonObject()
            }

            val header = context.serialize(src.header)
            val payload = context.serialize(src.payload)

            val headerEnc = Base64.encodeToString(header.asString.toByteArray(), Base64.DEFAULT)
            val payloadEnc = Base64.encodeToString(payload.asString.toByteArray(), Base64.DEFAULT)

            val signer = Signature.getInstance("SHA512withRSA", "SC")
            val parameter = PSSParameterSpec(MGF1ParameterSpec.SHA512.digestAlgorithm, "MGF1", MGF1ParameterSpec.SHA512, 64, 1)
            signer.setParameter(parameter)
            signer.initSign(signingKey)

            val signable = headerEnc + payloadEnc
            signer.update(signable.toByteArray())

            val signature = Base64.encodeToString(signer.sign(), Base64.DEFAULT)
            val jwt = signable + signature

            return JsonPrimitive(jwt)
        }

        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): DMEJsonWebToken {

            if (context == null) throw IllegalArgumentException()

            val jwt = json?.asJsonPrimitive ?: throw IllegalArgumentException()

            val jwtComponents = jwt.asString.split(".")
            if (jwtComponents.count() != 3) throw IllegalArgumentException()

            val headerEnc = jwtComponents[0]
            val payloadEnc = jwtComponents[1]
            val signatureEnc = jwtComponents[2]

            val headerJson = Gson().toJsonTree(String(Base64.decode(headerEnc, Base64.DEFAULT)))
            val payloadJson = Gson().toJsonTree(String(Base64.decode(payloadEnc, Base64.DEFAULT)))

            val header = context.deserialize<Header>(headerJson, object: TypeToken<Header>(){}.type)
            val payload = context.deserialize<Payload.OAuth>(payloadJson, object: TypeToken<Payload.OAuth>(){}.type)
            val signature = JsonPrimitive(String(Base64.decode(signatureEnc, Base64.DEFAULT))).asString

            return DMEJsonWebToken(header, payload, signature)
        }
    }

    internal data class Header (

        @SerializedName("alg")
        val signatureAlgorithm: String,

        @SerializedName("typ")
        val type: String,

        @SerializedName("kid")
        val keyIdentifier: String?,

        @SerializedName("jku")
        val jsonKeysetUrl: String?
    )

    internal sealed class Payload {

        data class OAuth (

            @SerializedName("access_token")
            val accessToken: String,

            @SerializedName("refresh_token")
            val refreshToken: String,

            @SerializedName("expires_on")
            val accessTokenExpiry: Date,

            @SerializedName("token_type")
            val tokenType: String

        ): Payload()

        data class AuthCodeRedemption (

            @SerializedName("client_id")
            val contractId: String,

            @SerializedName("code")
            val authorizationCode: String,

            @SerializedName("code_verifier")
            val codeVerifier: String,

            @SerializedName("grant_type")
            val grantType: String,

            @SerializedName("redirect_uri")
            val redirectUri: String,

            val nonce: String,

            val timestamp: Double

        ): Payload()
    }
}