package ba.fluxor.fetchapi.network.http

data class HttpResponse(
  val statusCode: Int,
  val headers: Map<String, List<String>>,
  val cookies: List<HttpCookie>,
  val body: String,
)

data class HttpCookie(
  val name: String,
  val value: String,
  val domain: String?,
  val path: String?,
  val expires: String?,
  val httpOnly: Boolean,
  val secure: Boolean,
)
