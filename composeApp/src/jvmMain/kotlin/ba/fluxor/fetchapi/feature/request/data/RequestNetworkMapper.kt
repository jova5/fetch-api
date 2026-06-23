package ba.fluxor.fetchapi.feature.request.data

import ba.fluxor.fetchapi.network.http.HttpBody
import ba.fluxor.fetchapi.network.http.HttpMethod
import ba.fluxor.fetchapi.network.http.HttpRequest
import ba.fluxor.fetchapi.network.http.MultipartPart

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
      // multipart/form-data must carry a boundary; let the engine set Content-Type itself.
      val contentType = RequestHeaderDerivation.contentTypeFor(request.body)
      if (request.body !is BodyConfig.FormData &&
        contentType.isNotEmpty() && "Content-Type" !in excludedAutoHeaders) {
        putIfAbsent("Content-Type", contentType)
      }
      RequestHeaderDerivation.staticAutoHeaders().forEach { (k, v) ->
        if (k !in excludedAutoHeaders) putIfAbsent(k, v)
      }
    }.filterValues { it.isNotEmpty() }

    return HttpRequest(
      url = request.url,
      method = HttpMethod.valueOf(request.method.uppercase()),
      headers = mergedHeaders,
      body = encodeBody(request.body),
    )
  }

  private fun encodeBody(body: BodyConfig): HttpBody? = when (body) {
    BodyConfig.None, is BodyConfig.Binary -> null
    is BodyConfig.Raw -> body.content.ifBlank { null }?.let(HttpBody::Text)
    is BodyConfig.UrlEncoded -> body.fields
      .filter { it.enabled && it.key.isNotBlank() }
      .joinToString("&") {
        java.net.URLEncoder.encode(it.key, "UTF-8") + "=" + java.net.URLEncoder.encode(it.value, "UTF-8")
      }
      .ifBlank { null }
      ?.let(HttpBody::Text)
    is BodyConfig.FormData -> encodeFormData(body)
  }

  private fun encodeFormData(body: BodyConfig.FormData): HttpBody? {
    val parts = body.fields
      .filter { it.enabled && it.key.isNotBlank() }
      .mapNotNull { entry ->
        if (entry.isFile) {
          entry.filePaths.ifEmpty { null }?.let { MultipartPart.File(entry.key, it) }
        } else {
          MultipartPart.Text(entry.key, entry.value)
        }
      }
    return parts.ifEmpty { null }?.let(HttpBody::Multipart)
  }
}
