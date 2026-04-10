package ba.fluxor.fetchapi.network.graphql

import ba.fluxor.fetchapi.network.http.HttpResponse
import ba.fluxor.fetchapi.network.http.httpPost
import kotlinx.serialization.json.Json

suspend fun graphql(
    url: String,
    query: String,
    variables: Map<String, String>? = null,
    operationName: String? = null,
    headers: Map<String, String> = emptyMap(),
): HttpResponse {
    val body = Json.encodeToString(
        GraphQlRequest(
            query = query,
            variables = variables,
            operationName = operationName,
        )
    )

    val graphQlHeaders = buildMap {
        put("Content-Type", "application/json")
        putAll(headers)
    }

    return httpPost(url = url, body = body, headers = graphQlHeaders)
}
