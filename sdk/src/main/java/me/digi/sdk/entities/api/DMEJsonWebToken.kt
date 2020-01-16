package me.digi.sdk.entities.api

import android.util.Base64
import com.google.gson.*
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.net.URLEncoder
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.MGF1ParameterSpec
import java.security.spec.PSSParameterSpec
import java.util.*

internal class DMEJsonWebToken (

    val header: Header,
    val payload: Payload

) {

    fun tokenise(key: PrivateKey): String {

        val serialized = GsonBuilder()
            .registerTypeAdapter(DMEJsonWebToken::class.java, Adapter(key))
            .create()
            .toJsonTree(this)
            .asString

        return "Bearer $serialized"
    }

    internal class Adapter(val signingKey: PrivateKey): JsonSerializer<DMEJsonWebToken>, JsonDeserializer<DMEJsonWebToken> {
        override fun serialize(src: DMEJsonWebToken?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {

            if (src == null || context == null) {
                return JsonObject()
            }

            val jwtEncodingFlags = Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP

            val header = context.serialize(src.header)
            val payload = context.serialize(src.payload)

            val headerEnc = Base64.encodeToString(header.toString().toByteArray(), jwtEncodingFlags)
            val payloadEnc = Base64.encodeToString(payload.toString().toByteArray(), jwtEncodingFlags)

            val signer = Signature.getInstance("SHA512withRSA/PSS", "SC")
            val parameter = PSSParameterSpec(MGF1ParameterSpec.SHA512.digestAlgorithm, "MGF1", MGF1ParameterSpec.SHA512, 64, 1)
            signer.setParameter(parameter)
            signer.initSign(signingKey)

            val signable = "$headerEnc.$payloadEnc"
            signer.update(signable.toByteArray())

            val signature = Base64.encodeToString(signer.sign(),
                jwtEncodingFlags)
            val jwt = "$signable.$signature"

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

            return DMEJsonWebToken(header, payload)
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

        data class PreAuthRequest (

            @SerializedName("client_id")
            val clientId: String,

            @SerializedName("code_challenge")
            val codeChallenge: String,

            @SerializedName("code_challenge_method")
            val codeChallengeMethod: String,

            val state: String,

            @SerializedName("redirect_uri")
            val redirectUri: String,

            @SerializedName("response_mode")
            val responseMode: String,

            @SerializedName("response_type")
            val responseType: String,

            val nonce: String,

            val timestamp: Double

        ): Payload()
    }
}