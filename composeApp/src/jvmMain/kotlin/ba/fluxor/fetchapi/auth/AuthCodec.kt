package ba.fluxor.fetchapi.feature.sub_project.data.auth

import kotlinx.serialization.json.Json

object AuthTypes {
  const val NONE = "NONE"
  const val BASIC = "BASIC AUTH"
  const val BEARER = "BEARER TOKEN"
  const val JWT = "JWT BEARER"
  const val DIGEST = "DIGEST AUTH"
  const val OAUTH1 = "OAUTH 1.0"
  const val OAUTH2 = "OAUTH 2.0"
  const val API_KEY = "API_KEY"
  const val CUSTOM = "CUSTOM"

  val ALL = listOf(NONE, BASIC, BEARER, JWT, DIGEST, OAUTH1, OAUTH2, API_KEY, CUSTOM)
}

object AuthCodec {

  private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
  }

  fun encode(auth: Auth): Pair<String, String?> = when (auth) {
    Auth.None -> AuthTypes.NONE to null
    is Auth.Basic -> AuthTypes.BASIC to json.encodeToString(Auth.Basic.serializer(), auth)
    is Auth.Bearer -> AuthTypes.BEARER to json.encodeToString(Auth.Bearer.serializer(), auth)
    is Auth.Jwt -> AuthTypes.JWT to json.encodeToString(Auth.Jwt.serializer(), auth)
    is Auth.Digest -> AuthTypes.DIGEST to json.encodeToString(Auth.Digest.serializer(), auth)
    is Auth.OAuth1 -> AuthTypes.OAUTH1 to json.encodeToString(Auth.OAuth1.serializer(), auth)
    is Auth.OAuth2 -> AuthTypes.OAUTH2 to json.encodeToString(Auth.OAuth2.serializer(), auth)
    is Auth.ApiKey -> AuthTypes.API_KEY to json.encodeToString(Auth.ApiKey.serializer(), auth)
    is Auth.Custom -> AuthTypes.CUSTOM to json.encodeToString(Auth.Custom.serializer(), auth)
  }

  fun decode(authType: String, authConfig: String?): Auth {
    return when (authType) {
      AuthTypes.NONE -> Auth.None
      AuthTypes.BASIC -> tryDecode(authConfig, Auth.Basic.serializer()) ?: Auth.Basic()
      AuthTypes.BEARER -> tryDecode(authConfig, Auth.Bearer.serializer()) ?: Auth.Bearer()
      AuthTypes.JWT -> tryDecode(authConfig, Auth.Jwt.serializer()) ?: Auth.Jwt()
      AuthTypes.DIGEST -> tryDecode(authConfig, Auth.Digest.serializer()) ?: Auth.Digest()
      AuthTypes.OAUTH1 -> tryDecode(authConfig, Auth.OAuth1.serializer()) ?: Auth.OAuth1()
      AuthTypes.OAUTH2 -> tryDecode(authConfig, Auth.OAuth2.serializer()) ?: Auth.OAuth2()
      AuthTypes.API_KEY -> tryDecode(authConfig, Auth.ApiKey.serializer()) ?: Auth.ApiKey()
      AuthTypes.CUSTOM -> tryDecode(authConfig, Auth.Custom.serializer())
        ?: Auth.Custom(raw = authConfig.orEmpty())
      else -> Auth.None
    }
  }

  fun defaultConfigFor(authType: String): String? = encode(decode(authType, null)).second

  private fun <T> tryDecode(raw: String?, serializer: kotlinx.serialization.KSerializer<T>): T? {
    if (raw.isNullOrBlank()) return null
    return try {
      json.decodeFromString(serializer, raw)
    } catch (_: Throwable) {
      null
    }
  }
}
