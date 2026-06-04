package ba.fluxor.fetchapi.network.http

import ba.fluxor.fetchapi.network.NetworkEngine
import ba.fluxor.fetchapi.network.NetworkExecutor
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpMethod as KtorHttpMethod

object HttpEngine : NetworkEngine<HttpRequest, HttpResponse> {

    private val client: HttpClient = HttpClient(Java) {
        expectSuccess = false
        engine {
            config {
                executor(NetworkExecutor.executor)
                followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
            }
        }
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
            if (request.body != null) {
                setBody(request.body)
            }
        }

        return HttpResponse(
            statusCode = response.status.value,
            headers = response.headers.entries().associate { it.key to it.value },
            body = response.bodyAsText(),
        )
    }
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
