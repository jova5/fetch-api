package ba.fluxor.fetchapi.network.http

data class HttpResponse(
  val statusCode: Int,
  val headers: Map<String, List<String>>,
  val body: String,
)
