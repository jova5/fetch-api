package ba.fluxor.fetchapi.feature.sub_project.data.auth

import kotlinx.serialization.json.Json

object SubProjectAuthTypes {
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

object SubProjectAuthCodec {

  private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
  }

  fun encode(auth: SubProjectAuth): Pair<String, String?> = when (auth) {
    SubProjectAuth.None -> SubProjectAuthTypes.NONE to null
    is SubProjectAuth.Basic -> SubProjectAuthTypes.BASIC to json.encodeToString(SubProjectAuth.Basic.serializer(), auth)
    is SubProjectAuth.Bearer -> SubProjectAuthTypes.BEARER to json.encodeToString(SubProjectAuth.Bearer.serializer(), auth)
    is SubProjectAuth.Jwt -> SubProjectAuthTypes.JWT to json.encodeToString(SubProjectAuth.Jwt.serializer(), auth)
    is SubProjectAuth.Digest -> SubProjectAuthTypes.DIGEST to json.encodeToString(SubProjectAuth.Digest.serializer(), auth)
    is SubProjectAuth.OAuth1 -> SubProjectAuthTypes.OAUTH1 to json.encodeToString(SubProjectAuth.OAuth1.serializer(), auth)
    is SubProjectAuth.OAuth2 -> SubProjectAuthTypes.OAUTH2 to json.encodeToString(SubProjectAuth.OAuth2.serializer(), auth)
    is SubProjectAuth.ApiKey -> SubProjectAuthTypes.API_KEY to json.encodeToString(SubProjectAuth.ApiKey.serializer(), auth)
    is SubProjectAuth.Custom -> SubProjectAuthTypes.CUSTOM to json.encodeToString(SubProjectAuth.Custom.serializer(), auth)
  }

  fun decode(authType: String, authConfig: String?): SubProjectAuth {
    return when (authType) {
      SubProjectAuthTypes.NONE -> SubProjectAuth.None
      SubProjectAuthTypes.BASIC -> tryDecode(authConfig, SubProjectAuth.Basic.serializer()) ?: SubProjectAuth.Basic()
      SubProjectAuthTypes.BEARER -> tryDecode(authConfig, SubProjectAuth.Bearer.serializer()) ?: SubProjectAuth.Bearer()
      SubProjectAuthTypes.JWT -> tryDecode(authConfig, SubProjectAuth.Jwt.serializer()) ?: SubProjectAuth.Jwt()
      SubProjectAuthTypes.DIGEST -> tryDecode(authConfig, SubProjectAuth.Digest.serializer()) ?: SubProjectAuth.Digest()
      SubProjectAuthTypes.OAUTH1 -> tryDecode(authConfig, SubProjectAuth.OAuth1.serializer()) ?: SubProjectAuth.OAuth1()
      SubProjectAuthTypes.OAUTH2 -> tryDecode(authConfig, SubProjectAuth.OAuth2.serializer()) ?: SubProjectAuth.OAuth2()
      SubProjectAuthTypes.API_KEY -> tryDecode(authConfig, SubProjectAuth.ApiKey.serializer()) ?: SubProjectAuth.ApiKey()
      SubProjectAuthTypes.CUSTOM -> tryDecode(authConfig, SubProjectAuth.Custom.serializer())
        ?: SubProjectAuth.Custom(raw = authConfig.orEmpty())
      else -> SubProjectAuth.None
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
