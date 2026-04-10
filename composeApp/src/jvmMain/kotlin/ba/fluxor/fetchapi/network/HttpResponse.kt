package ba.fluxor.fetchapi.network

data class HttpResponse(
  val statusCode: Int,
  val headers: Map<String, List<String>>,
  val body: String,
)
