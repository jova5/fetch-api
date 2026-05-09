package ba.fluxor.fetchapi.feature.request.data

import ba.fluxor.fetchapi.feature.sub_project.data.auth.Auth
import ba.fluxor.fetchapi.feature.sub_project.data.auth.AuthCodec
import ba.fluxor.fetchapi.network.http.HttpMethod
import ba.fluxor.fetchapi.network.http.HttpRequest
import java.util.Base64

object RequestNetworkMapper {

  fun toHttpRequest(
    request: Request,
    parentAuthType: String? = null,
    parentAuthConfig: String? = null,
  ): HttpRequest {
    val (resolvedAuthType, resolvedAuthConfig) = resolveAuth(
      request.authType,
      request.authConfig,
      parentAuthType,
      parentAuthConfig,
    )
    val auth = AuthCodec.decode(resolvedAuthType, resolvedAuthConfig)

    val userHeaders = request.headers
      .filter { it.enabled && it.key.isNotBlank() }
      .associate { it.key to it.value }

    val authHeaders = headersFor(auth)

    val mergedHeaders = LinkedHashMap<String, String>().apply {
      putAll(authHeaders)
      putAll(userHeaders)
      putIfAbsent("Content-Type", contentTypeFor(request.body))
    }.filterValues { it.isNotEmpty() }

    val bodyString = encodeBody(request.body)

    return HttpRequest(
      url = request.url,
      method = HttpMethod.valueOf(request.method.uppercase()),
      headers = mergedHeaders,
      body = bodyString,
    )
  }

  private fun resolveAuth(
    requestAuthType: String,
    requestAuthConfig: String?,
    parentAuthType: String?,
    parentAuthConfig: String?,
  ): Pair<String, String?> = when {
    requestAuthType == "INHERIT" && parentAuthType != null -> parentAuthType to parentAuthConfig
    requestAuthType == "INHERIT" -> "NONE" to null
    else -> requestAuthType to requestAuthConfig
  }

  private fun headersFor(auth: Auth): Map<String, String> = when (auth) {
    Auth.None -> emptyMap()
    is Auth.Bearer -> mapOf("Authorization" to "Bearer ${auth.token}")
    is Auth.Basic -> {
      val encoded = Base64.getEncoder().encodeToString("${auth.username}:${auth.password}".toByteArray())
      mapOf("Authorization" to "Basic $encoded")
    }
    is Auth.ApiKey -> if (auth.addTo == Auth.AddTo.HEADER) mapOf(auth.key to auth.value) else emptyMap()
    is Auth.OAuth2 -> mapOf("Authorization" to "${auth.headerPrefix} ${auth.accessToken}")
    else -> emptyMap()
  }

  private fun contentTypeFor(body: BodyConfig): String = when (body) {
    BodyConfig.None -> ""
    is BodyConfig.Raw -> body.language.mime
    is BodyConfig.FormData -> "multipart/form-data"
    is BodyConfig.UrlEncoded -> "application/x-www-form-urlencoded"
    is BodyConfig.Binary -> "application/octet-stream"
  }

  private fun encodeBody(body: BodyConfig): String? = when (body) {
    BodyConfig.None -> null
    is BodyConfig.Raw -> body.content.ifBlank { null }
    is BodyConfig.UrlEncoded -> body.fields
      .filter { it.enabled && it.key.isNotBlank() }
      .joinToString("&") {
        java.net.URLEncoder.encode(it.key, "UTF-8") + "=" + java.net.URLEncoder.encode(it.value, "UTF-8")
      }
      .ifBlank { null }
    is BodyConfig.FormData, is BodyConfig.Binary -> null
  }
}
