package ba.fluxor.fetchapi.feature.sub_project.data.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class SubProjectAuth {

  @Serializable
  enum class AddTo { HEADER, QUERY }

  @Serializable
  @SerialName("NONE")
  data object None : SubProjectAuth()

  @Serializable
  @SerialName("BASIC")
  data class Basic(
    val username: String = "",
    val password: String = "",
  ) : SubProjectAuth()

  @Serializable
  @SerialName("BEARER")
  data class Bearer(
    val token: String = "",
  ) : SubProjectAuth()

  @Serializable
  @SerialName("JWT")
  data class Jwt(
    val algorithm: String = "HS256",
    val secret: String = "",
    val secretBase64Encoded: Boolean = false,
    val payload: String = "{}",
    val headerPrefix: String = "Bearer",
    val addTo: AddTo = AddTo.HEADER,
    val queryParamKey: String = "token",
  ) : SubProjectAuth()

  @Serializable
  @SerialName("DIGEST")
  data class Digest(
    val username: String = "",
    val password: String = "",
    val realm: String = "",
    val nonce: String = "",
    val algorithm: String = "MD5",
    val qop: String = "",
    val nonceCount: String = "",
    val clientNonce: String = "",
    val opaque: String = "",
  ) : SubProjectAuth()

  @Serializable
  @SerialName("OAUTH1")
  data class OAuth1(
    val consumerKey: String = "",
    val consumerSecret: String = "",
    val token: String = "",
    val tokenSecret: String = "",
    val signatureMethod: String = "HMAC-SHA1",
    val timestamp: String = "",
    val nonce: String = "",
    val version: String = "1.0",
    val realm: String = "",
    val callback: String = "",
    val verifier: String = "",
    val includeBodyHash: Boolean = false,
    val addEmptyParamsToSignature: Boolean = false,
    val addTo: AddTo = AddTo.HEADER,
  ) : SubProjectAuth()

  @Serializable
  @SerialName("OAUTH2")
  data class OAuth2(
    val accessToken: String = "",
    val headerPrefix: String = "Bearer",
    val grantType: String = "AUTHORIZATION_CODE",
    val callbackUrl: String = "",
    val authUrl: String = "",
    val accessTokenUrl: String = "",
    val clientId: String = "",
    val clientSecret: String = "",
    val scope: String = "",
    val state: String = "",
    val clientAuthentication: String = "BODY",
    val username: String = "",
    val password: String = "",
    val addTo: AddTo = AddTo.HEADER,
  ) : SubProjectAuth()

  @Serializable
  @SerialName("API_KEY")
  data class ApiKey(
    val key: String = "",
    val value: String = "",
    val addTo: AddTo = AddTo.HEADER,
  ) : SubProjectAuth()

  @Serializable
  @SerialName("CUSTOM")
  data class Custom(
    val raw: String = "",
  ) : SubProjectAuth()
}
