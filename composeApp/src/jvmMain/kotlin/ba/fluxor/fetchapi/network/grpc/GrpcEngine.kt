package ba.fluxor.fetchapi.network.grpc

import ba.fluxor.fetchapi.network.NetworkExecutor
import ba.fluxor.fetchapi.network.grpc.proto.ProtoFile
import ba.fluxor.fetchapi.network.grpc.proto.ProtoRpcType
import ba.fluxor.fetchapi.network.grpc.proto.toFileDescriptor
import com.google.protobuf.DynamicMessage
import com.google.protobuf.util.JsonFormat
import io.grpc.CallOptions
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.ProtoUtils
import io.grpc.stub.ClientCalls
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object GrpcEngine {

    suspend fun execute(request: GrpcRequest): GrpcResponse {
        val fileDescriptor = request.protoFile.toFileDescriptor()

        val serviceDescriptor = fileDescriptor.services
            .find { it.name == request.serviceName || it.fullName == request.serviceName }
            ?: throw IllegalArgumentException(
                "Service '${request.serviceName}' not found. Available: ${fileDescriptor.services.map { it.fullName }}"
            )

        val methodDescriptor = serviceDescriptor.methods
            .find { it.name == request.methodName }
            ?: throw IllegalArgumentException(
                "Method '${request.methodName}' not found in service '${request.serviceName}'. " +
                    "Available: ${serviceDescriptor.methods.map { it.name }}"
            )

        // Check for streaming (not yet supported)
        val protoService = request.protoFile.services.find { it.name == request.serviceName }
        val protoRpc = protoService?.rpcs?.find { it.name == request.methodName }
        if (protoRpc != null && protoRpc.type != ProtoRpcType.UNARY) {
            throw UnsupportedOperationException(
                "Streaming calls (${protoRpc.type}) are not yet supported. Only UNARY calls are available."
            )
        }

        val inputType = methodDescriptor.inputType
        val outputType = methodDescriptor.outputType

        // Parse request JSON into DynamicMessage
        val requestMessage = DynamicMessage.newBuilder(inputType).apply {
            if (request.requestJson.isNotBlank()) {
                JsonFormat.parser().merge(request.requestJson, this)
            }
        }.build()

        // Build gRPC method descriptor
        val grpcMethod = io.grpc.MethodDescriptor.newBuilder<DynamicMessage, DynamicMessage>()
            .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
            .setFullMethodName("${serviceDescriptor.fullName}/${request.methodName}")
            .setRequestMarshaller(ProtoUtils.marshaller(DynamicMessage.getDefaultInstance(inputType)))
            .setResponseMarshaller(ProtoUtils.marshaller(DynamicMessage.getDefaultInstance(outputType)))
            .build()

        val channel = buildChannel(request)

        try {
            val startTime = System.nanoTime()

            val callOptions = CallOptions.DEFAULT
                .withDeadlineAfter(request.deadlineMs, TimeUnit.MILLISECONDS)

            try {
                val responseMessage = withContext(NetworkExecutor.dispatcher) {
                    val call = channel.newCall(grpcMethod, callOptions)
                    ClientCalls.blockingUnaryCall(call, requestMessage)
                }

                val durationMs = (System.nanoTime() - startTime) / 1_000_000
                val responseJson = JsonFormat.printer().print(responseMessage)

                return GrpcResponse(
                    statusCode = 0,
                    statusName = "OK",
                    body = responseJson,
                    durationMs = durationMs,
                )
            } catch (e: StatusRuntimeException) {
                val durationMs = (System.nanoTime() - startTime) / 1_000_000
                return GrpcResponse(
                    statusCode = e.status.code.value(),
                    statusName = e.status.code.name,
                    body = e.status.description ?: e.message ?: "",
                    metadata = extractMetadata(e.trailers),
                    durationMs = durationMs,
                )
            }
        } finally {
            channel.shutdown()
        }
    }

    private fun buildChannel(request: GrpcRequest): ManagedChannel =
        ManagedChannelBuilder.forAddress(request.host, request.port)
            .apply { if (!request.useTls) usePlaintext() }
            .executor(NetworkExecutor.executor)
            .build()

    private fun extractMetadata(trailers: Metadata?): Map<String, String> {
        if (trailers == null) return emptyMap()
        val map = mutableMapOf<String, String>()
        for (key in trailers.keys()) {
            if (!key.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                val metaKey = Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER)
                trailers.get(metaKey)?.let { map[key] = it }
            }
        }
        return map
    }
}

suspend fun grpcCall(
    host: String,
    port: Int,
    serviceName: String,
    methodName: String,
    requestJson: String,
    protoFile: ProtoFile,
    useTls: Boolean = false,
    metadata: Map<String, String> = emptyMap(),
    deadlineMs: Long = 10_000,
): GrpcResponse = GrpcEngine.execute(
    GrpcRequest(
        host = host,
        port = port,
        serviceName = serviceName,
        methodName = methodName,
        requestJson = requestJson,
        protoFile = protoFile,
        useTls = useTls,
        metadata = metadata,
        deadlineMs = deadlineMs,
    )
)
