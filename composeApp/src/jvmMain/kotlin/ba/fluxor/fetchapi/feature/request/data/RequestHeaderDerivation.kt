package ba.fluxor.fetchapi.feature.request.data

import ba.fluxor.fetchapi.feature.sub_project.data.auth.Auth
import ba.fluxor.fetchapi.feature.sub_project.data.auth.AuthCodec
import java.util.*

object RequestHeaderDerivation {

  fun resolveAuth(
    requestAuthType: String,
    requestAuthConfig: String?,
    parentAuthType: String?,
    parentAuthConfig: String?,
  ): Pair<String, String?> = when {
    requestAuthType == "INHERIT" && parentAuthType != null -> parentAuthType to parentAuthConfig
    requestAuthType == "INHERIT" -> "NONE" to null
    else -> requestAuthType to requestAuthConfig
  }

  fun resolvedAuth(
    requestAuthType: String,
    requestAuthConfig: String?,
    parentAuthType: String?,
    parentAuthConfig: String?,
  ): Auth {
    val (type, config) = resolveAuth(requestAuthType, requestAuthConfig, parentAuthType,
      parentAuthConfig)
    return AuthCodec.decode(type, config)
  }

  fun authHeaders(auth: Auth): Map<String, String> = when (auth) {
    Auth.None -> emptyMap()
    is Auth.Bearer -> mapOf("Authorization" to "Bearer ${auth.token}")
    is Auth.Basic -> {
      val encoded = Base64.getEncoder()
        .encodeToString("${auth.username}:${auth.password}".toByteArray())
      mapOf("Authorization" to "Basic $encoded")
    }

    is Auth.ApiKey -> if (auth.addTo == Auth.AddTo.HEADER) mapOf(
      auth.key to auth.value) else emptyMap()

    is Auth.OAuth2 -> mapOf("Authorization" to "${auth.headerPrefix} ${auth.accessToken}")
    else -> emptyMap()
  }

  fun contentTypeFor(body: BodyConfig): String = when (body) {
    BodyConfig.None -> ""
    is BodyConfig.Raw -> body.language.mime
    is BodyConfig.FormData -> "multipart/form-data"
    is BodyConfig.UrlEncoded -> "application/x-www-form-urlencoded"
    is BodyConfig.Binary -> "application/octet-stream"
  }

  fun staticAutoHeaders(): Map<String, String> = linkedMapOf(
    "User-Agent" to "FetchAPI/1.0",
    "Accept" to "*/*",
    "Connection" to "keep-alive",
  )
}
