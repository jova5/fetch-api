package ba.fluxor.fetchapi.network.http

data class HttpRequest(
    val url: String,
    val method: HttpMethod = HttpMethod.GET,
    val headers: Map<String, String> = emptyMap(),
    val body: HttpBody? = null,
)

/** Engine-agnostic request body. Keeps Ktor types out of the mapper layer. */
sealed interface HttpBody {

    /** A plain string body (raw / url-encoded). */
    data class Text(val content: String) : HttpBody

    /** A `multipart/form-data` body; the engine sets the Content-Type (with boundary) itself. */
    data class Multipart(val parts: List<MultipartPart>) : HttpBody
}

/** A single part of a [HttpBody.Multipart] body. */
sealed interface MultipartPart {

    /** A text field. */
    data class Text(val name: String, val value: String) : MultipartPart

    /** A file field. [paths] are sent as separate parts under the same [name]. */
    data class File(val name: String, val paths: List<String>) : MultipartPart
}
