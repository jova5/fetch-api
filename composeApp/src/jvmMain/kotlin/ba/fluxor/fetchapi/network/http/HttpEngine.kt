package ba.fluxor.fetchapi.network.http

import ba.fluxor.fetchapi.network.NetworkEngine
import ba.fluxor.fetchapi.network.NetworkExecutor
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.io.File
import java.nio.file.Files
import kotlinx.io.asSource
import kotlinx.io.buffered
import io.ktor.http.HttpMethod as KtorHttpMethod

object HttpEngine : NetworkEngine<HttpRequest, HttpResponse> {

  private val client: HttpClient = HttpClient(Java) {
    expectSuccess = false
    engine {
      config {
        executor(NetworkExecutor.executor)
        // Let Ktor (not the JDK engine) follow redirects so the response pipeline —
        // and the cookie jar below — can observe Set-Cookie on intermediate 3xx hops.
        followRedirects(java.net.http.HttpClient.Redirect.NEVER)
      }
    }
    install(HttpCookies)
    install(Logging) {
      level = LogLevel.NONE
    }
    install(ContentEncoding) {
      gzip()
      deflate()
    }
  }

  override suspend fun execute(request: HttpRequest): HttpResponse {
    val response = client.request(request.url) {
      method = KtorHttpMethod.parse(request.method.name)
      request.headers.forEach { (name, value) -> header(name, value) }
      when (val body = request.body) {
        null -> Unit
        is HttpBody.Text -> setBody(body.content)
        is HttpBody.Multipart -> setBody(MultiPartFormDataContent(buildFormData(body.parts)))
      }
    }

    // Read from the cookie jar (not the final response headers): it has accumulated every
    // Set-Cookie across the redirect chain, whereas the final 200 may carry none.
    val cookies = client.cookies(request.url)
      .map { cookie ->
        HttpCookie(
          name = cookie.name,
          value = cookie.value,
          domain = cookie.domain,
          path = cookie.path,
          expires = cookie.expires?.toHttpDate(),
          httpOnly = cookie.httpOnly,
          secure = cookie.secure,
        )
      }

    return HttpResponse(
      statusCode = response.status.value,
      headers = response.headers.entries()
        .associate { it.key to it.value },
      cookies = cookies,
      body = response.bodyAsText(),
    )
  }
}

/** Maps the engine-agnostic multipart parts into Ktor's [formData] builder output. */
private fun buildFormData(parts: List<MultipartPart>) = formData {
  parts.forEach { part ->
    when (part) {
      is MultipartPart.Text -> append(
        part.name,
        part.value.toByteArray(Charsets.UTF_8),
        Headers.build {
          append(HttpHeaders.ContentType, "${ContentType.Text.Plain}; charset=UTF-8")
        },
      )

      is MultipartPart.File -> part.paths.forEach { path ->
        val file = File(path)
        val contentType = runCatching { Files.probeContentType(file.toPath()) }
          .getOrNull()
          ?.let { ContentType.parse(it) }
          ?: ContentType.Application.OctetStream
        append(
          part.name,
          InputProvider(file.length()) {
            file.inputStream()
              .asSource()
              .buffered()
          },
          Headers.build {
            append(HttpHeaders.ContentType, contentType.toString())
            append(
              HttpHeaders.ContentDisposition,
              "filename=\"${file.name}\"",
            )
          },
        )
      }
    }
  }
}

suspend fun httpGet(url: String, headers: Map<String, String> = emptyMap()): HttpResponse =
  HttpEngine.execute(HttpRequest(url = url, method = HttpMethod.GET, headers = headers))

suspend fun httpPost(url: String, body: String? = null,
  headers: Map<String, String> = emptyMap()): HttpResponse =
  HttpEngine.execute(HttpRequest(url = url, method = HttpMethod.POST, headers = headers,
    body = body?.let(HttpBody::Text)))

suspend fun httpPut(url: String, body: String? = null,
  headers: Map<String, String> = emptyMap()): HttpResponse =
  HttpEngine.execute(HttpRequest(url = url, method = HttpMethod.PUT, headers = headers,
    body = body?.let(HttpBody::Text)))

suspend fun httpPatch(url: String, body: String? = null,
  headers: Map<String, String> = emptyMap()): HttpResponse =
  HttpEngine.execute(HttpRequest(url = url, method = HttpMethod.PATCH, headers = headers,
    body = body?.let(HttpBody::Text)))

suspend fun httpDelete(url: String, body: String? = null,
  headers: Map<String, String> = emptyMap()): HttpResponse =
  HttpEngine.execute(HttpRequest(url = url, method = HttpMethod.DELETE, headers = headers,
    body = body?.let(HttpBody::Text)))

suspend fun httpHead(url: String, headers: Map<String, String> = emptyMap()): HttpResponse =
  HttpEngine.execute(HttpRequest(url = url, method = HttpMethod.HEAD, headers = headers))

suspend fun httpOptions(url: String, headers: Map<String, String> = emptyMap()): HttpResponse =
  HttpEngine.execute(HttpRequest(url = url, method = HttpMethod.OPTIONS, headers = headers))
