package ba.fluxor.fetchapi.network.grpc.proto

data class ProtoFile(
    val syntax: String = "proto3",
    val packageName: String = "",
    val options: Map<String, String> = emptyMap(),
    val imports: List<String> = emptyList(),
    val messages: List<ProtoMessage> = emptyList(),
    val enums: List<ProtoEnum> = emptyList(),
    val services: List<ProtoService> = emptyList(),
)

data class ProtoMessage(
    val name: String,
    val fields: List<ProtoField> = emptyList(),
    val nestedMessages: List<ProtoMessage> = emptyList(),
    val nestedEnums: List<ProtoEnum> = emptyList(),
    val oneofs: List<ProtoOneof> = emptyList(),
)

data class ProtoField(
    val name: String,
    val typeName: String,
    val number: Int,
    val label: ProtoFieldLabel = ProtoFieldLabel.OPTIONAL,
    val mapKeyType: String? = null,
    val mapValueType: String? = null,
)

enum class ProtoFieldLabel {
    OPTIONAL,
    REPEATED,
    REQUIRED,
    MAP,
}

data class ProtoOneof(
    val name: String,
    val fields: List<ProtoField> = emptyList(),
)

data class ProtoEnum(
    val name: String,
    val values: List<ProtoEnumValue> = emptyList(),
)

data class ProtoEnumValue(
    val name: String,
    val number: Int,
)

data class ProtoService(
    val name: String,
    val rpcs: List<ProtoRpc> = emptyList(),
)

data class ProtoRpc(
    val name: String,
    val inputType: String,
    val outputType: String,
    val clientStreaming: Boolean = false,
    val serverStreaming: Boolean = false,
) {
    val type: ProtoRpcType
        get() = when {
            !clientStreaming && !serverStreaming -> ProtoRpcType.UNARY
            !clientStreaming && serverStreaming -> ProtoRpcType.SERVER_STREAMING
            clientStreaming && !serverStreaming -> ProtoRpcType.CLIENT_STREAMING
            else -> ProtoRpcType.BIDI_STREAMING
        }
}

enum class ProtoRpcType {
    UNARY,
    SERVER_STREAMING,
    CLIENT_STREAMING,
    BIDI_STREAMING,
}

class ProtoParseException(
    message: String,
    val line: Int = 0,
    val column: Int = 0,
) : RuntimeException("$message (line $line, column $column)")

sealed class ProtoToken(val line: Int, val column: Int) {
    class Identifier(val value: String, line: Int, column: Int) : ProtoToken(line, column)
    class StringLiteral(val value: String, line: Int, column: Int) : ProtoToken(line, column)
    class IntLiteral(val value: Int, line: Int, column: Int) : ProtoToken(line, column)
    class Symbol(val char: Char, line: Int, column: Int) : ProtoToken(line, column)
    class Eof(line: Int, column: Int) : ProtoToken(line, column)
}
