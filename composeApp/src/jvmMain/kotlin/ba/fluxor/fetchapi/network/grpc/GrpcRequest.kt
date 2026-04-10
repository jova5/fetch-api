package ba.fluxor.fetchapi.network.grpc

import ba.fluxor.fetchapi.network.grpc.proto.ProtoFile

data class GrpcRequest(
    val host: String,
    val port: Int,
    val serviceName: String,
    val methodName: String,
    val requestJson: String,
    val protoFile: ProtoFile,
    val useTls: Boolean = false,
    val metadata: Map<String, String> = emptyMap(),
    val deadlineMs: Long = 10_000,
)
