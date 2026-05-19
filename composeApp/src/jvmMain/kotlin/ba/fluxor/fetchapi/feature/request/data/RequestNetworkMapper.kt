package ba.fluxor.fetchapi.feature.request.data

import ba.fluxor.fetchapi.network.http.HttpMethod
import ba.fluxor.fetchapi.network.http.HttpRequest

object RequestNetworkMapper {

  fun toHttpRequest(
    request: Request,
    parentAuthType: String? = null,
    parentAuthConfig: String? = null,
    excludedAutoHeaders: Set<String> = emptySet(),
  ): HttpRequest {
    val auth = RequestHeaderDerivation.resolvedAuth(
      request.authType,
      request.authConfig,
      parentAuthType,
      parentAuthConfig,
    )

    val userHeaders = request.headers
      .filter { it.enabled && it.key.isNotBlank() }
      .associate { it.key to it.value }

    val authHeaders = RequestHeaderDerivation.authHeaders(auth)
      .filterKeys { it !in excludedAutoHeaders }

    val mergedHeaders = LinkedHashMap<String, String>().apply {
      putAll(authHeaders)
      putAll(userHeaders)
      val contentType = RequestHeaderDerivation.contentTypeFor(request.body)
      if (contentType.isNotEmpty() && "Content-Type" !in excludedAutoHeaders) {
        putIfAbsent("Content-Type", contentType)
      }
      RequestHeaderDerivation.staticAutoHeaders().forEach { (k, v) ->
        if (k !in excludedAutoHeaders) putIfAbsent(k, v)
      }
    }.filterValues { it.isNotEmpty() }

    val bodyString = encodeBody(request.body)

    return HttpRequest(
      url = request.url,
      method = HttpMethod.valueOf(request.method.uppercase()),
      headers = mergedHeaders,
      body = bodyString,
    )
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
