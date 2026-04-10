package ba.fluxor.fetchapi.network

import kotlinx.coroutines.future.await
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest as JdkHttpRequest
import java.net.http.HttpResponse.BodyHandlers

object HttpEngine {

    private val client: HttpClient = HttpClient.newBuilder()
        .executor(NetworkExecutor.executor)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    suspend fun execute(request: HttpRequest): HttpResponse {
        val jdkRequest = request.toJdkRequest()
        val jdkResponse = client.sendAsync(jdkRequest, BodyHandlers.ofString()).await()
        return HttpResponse(
            statusCode = jdkResponse.statusCode(),
            headers = jdkResponse.headers().map(),
            body = jdkResponse.body(),
        )
    }
}

private fun HttpRequest.toJdkRequest(): JdkHttpRequest {
    val builder = JdkHttpRequest.newBuilder()
        .uri(URI.create(url))
        .method(method.name, bodyPublisher())

    headers.forEach { (name, value) -> builder.header(name, value) }

    return builder.build()
}

private fun HttpRequest.bodyPublisher(): JdkHttpRequest.BodyPublisher =
    if (body != null) {
        JdkHttpRequest.BodyPublishers.ofString(body)
    } else {
        JdkHttpRequest.BodyPublishers.noBody()
    }

suspend fun httpGet(url: String, headers: Map<String, String> = emptyMap()): HttpResponse =
    HttpEngine.execute(HttpRequest(url = url, method = HttpMethod.GET, headers = headers))

suspend fun httpPost(url: String, body: String? = null, headers: Map<String, String> = emptyMap()): HttpResponse =
    HttpEngine.execute(HttpRequest(url = url, method = HttpMethod.POST, headers = headers, body = body))

suspend fun httpPut(url: String, body: String? = null, headers: Map<String, String> = emptyMap()): HttpResponse =
    HttpEngine.execute(HttpRequest(url = url, method = HttpMethod.PUT, headers = headers, body = body))

suspend fun httpPatch(url: String, body: String? = null, headers: Map<String, String> = emptyMap()): HttpResponse =
    HttpEngine.execute(HttpRequest(url = url, method = HttpMethod.PATCH, headers = headers, body = body))

suspend fun httpDelete(url: String, body: String? = null, headers: Map<String, String> = emptyMap()): HttpResponse =
    HttpEngine.execute(HttpRequest(url = url, method = HttpMethod.DELETE, headers = headers, body = body))

suspend fun httpHead(url: String, headers: Map<String, String> = emptyMap()): HttpResponse =
    HttpEngine.execute(HttpRequest(url = url, method = HttpMethod.HEAD, headers = headers))

suspend fun httpOptions(url: String, headers: Map<String, String> = emptyMap()): HttpResponse =
    HttpEngine.execute(HttpRequest(url = url, method = HttpMethod.OPTIONS, headers = headers))
