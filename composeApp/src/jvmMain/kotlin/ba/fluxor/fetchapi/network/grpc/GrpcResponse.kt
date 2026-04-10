package ba.fluxor.fetchapi.network.grpc

data class GrpcResponse(
    val statusCode: Int,
    val statusName: String,
    val body: String,
    val metadata: Map<String, String> = emptyMap(),
    val durationMs: Long = 0,
)
