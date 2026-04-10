package ba.fluxor.fetchapi.network.grpc.proto

import com.google.protobuf.DescriptorProtos.*
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type
import com.google.protobuf.Descriptors

private val scalarTypeMap = mapOf(
    "double" to Type.TYPE_DOUBLE,
    "float" to Type.TYPE_FLOAT,
    "int32" to Type.TYPE_INT32,
    "int64" to Type.TYPE_INT64,
    "uint32" to Type.TYPE_UINT32,
    "uint64" to Type.TYPE_UINT64,
    "sint32" to Type.TYPE_SINT32,
    "sint64" to Type.TYPE_SINT64,
    "fixed32" to Type.TYPE_FIXED32,
    "fixed64" to Type.TYPE_FIXED64,
    "sfixed32" to Type.TYPE_SFIXED32,
    "sfixed64" to Type.TYPE_SFIXED64,
    "bool" to Type.TYPE_BOOL,
    "string" to Type.TYPE_STRING,
    "bytes" to Type.TYPE_BYTES,
)

private val mapKeyScalars = setOf(
    "int32", "int64", "uint32", "uint64", "sint32", "sint64",
    "fixed32", "fixed64", "sfixed32", "sfixed64", "bool", "string",
)

fun ProtoFile.toFileDescriptor(): Descriptors.FileDescriptor {
    val fileProto = FileDescriptorProto.newBuilder()
        .setSyntax(syntax)

    if (packageName.isNotEmpty()) {
        fileProto.setPackage(packageName)
    }

    for (enum in enums) {
        fileProto.addEnumType(enum.toEnumDescriptorProto())
    }
    for (message in messages) {
        fileProto.addMessageType(message.toDescriptorProto(packageName))
    }
    for (service in services) {
        fileProto.addService(service.toServiceDescriptorProto(packageName))
    }

    return Descriptors.FileDescriptor.buildFrom(fileProto.build(), arrayOf())
}

private fun ProtoEnum.toEnumDescriptorProto(): EnumDescriptorProto {
    val builder = EnumDescriptorProto.newBuilder().setName(name)
    for (value in values) {
        builder.addValue(
            EnumValueDescriptorProto.newBuilder()
                .setName(value.name)
                .setNumber(value.number)
        )
    }
    return builder.build()
}

private fun ProtoMessage.toDescriptorProto(packageName: String): DescriptorProto {
    val builder = DescriptorProto.newBuilder().setName(name)

    for (nested in nestedEnums) {
        builder.addEnumType(nested.toEnumDescriptorProto())
    }
    for (nested in nestedMessages) {
        builder.addNestedType(nested.toDescriptorProto(packageName))
    }

    // Synthesize map entry types
    for (field in fields) {
        if (field.label == ProtoFieldLabel.MAP && field.mapKeyType != null && field.mapValueType != null) {
            builder.addNestedType(buildMapEntryType(field))
        }
    }

    for ((oneofIndex, oneof) in oneofs.withIndex()) {
        builder.addOneofDecl(OneofDescriptorProto.newBuilder().setName(oneof.name))
        for (field in oneof.fields) {
            builder.addField(field.toFieldDescriptorProto(packageName, oneofIndex))
        }
    }

    for (field in fields) {
        builder.addField(field.toFieldDescriptorProto(packageName))
    }

    return builder.build()
}

private fun ProtoField.toFieldDescriptorProto(
    packageName: String,
    oneofIndex: Int? = null,
): FieldDescriptorProto {
    val builder = FieldDescriptorProto.newBuilder()
        .setName(name)
        .setNumber(number)

    if (oneofIndex != null) {
        builder.setOneofIndex(oneofIndex)
    }

    when (label) {
        ProtoFieldLabel.MAP -> {
            builder.setLabel(Label.LABEL_REPEATED)
            builder.setType(Type.TYPE_MESSAGE)
            val entryName = name.replaceFirstChar { it.uppercase() } + "Entry"
            builder.setTypeName(entryName)
        }
        ProtoFieldLabel.REPEATED -> builder.setLabel(Label.LABEL_REPEATED)
        ProtoFieldLabel.REQUIRED -> builder.setLabel(Label.LABEL_REQUIRED)
        ProtoFieldLabel.OPTIONAL -> builder.setLabel(Label.LABEL_OPTIONAL)
    }

    if (label != ProtoFieldLabel.MAP) {
        val scalarType = scalarTypeMap[typeName]
        if (scalarType != null) {
            builder.setType(scalarType)
        } else {
            // Message or enum reference
            builder.setType(Type.TYPE_MESSAGE)
            builder.setTypeName(resolveTypeName(typeName, packageName))
        }
    }

    return builder.build()
}

private fun buildMapEntryType(field: ProtoField): DescriptorProto {
    val entryName = field.name.replaceFirstChar { it.uppercase() } + "Entry"
    val keyType = field.mapKeyType!!
    val valueType = field.mapValueType!!

    val keyField = FieldDescriptorProto.newBuilder()
        .setName("key")
        .setNumber(1)
        .setLabel(Label.LABEL_OPTIONAL)

    val keyScalar = scalarTypeMap[keyType]
    if (keyScalar != null) {
        keyField.setType(keyScalar)
    } else {
        keyField.setType(Type.TYPE_STRING)
    }

    val valueField = FieldDescriptorProto.newBuilder()
        .setName("value")
        .setNumber(2)
        .setLabel(Label.LABEL_OPTIONAL)

    val valueScalar = scalarTypeMap[valueType]
    if (valueScalar != null) {
        valueField.setType(valueScalar)
    } else {
        valueField.setType(Type.TYPE_MESSAGE)
        valueField.setTypeName(valueType)
    }

    return DescriptorProto.newBuilder()
        .setName(entryName)
        .addField(keyField)
        .addField(valueField)
        .setOptions(MessageOptions.newBuilder().setMapEntry(true))
        .build()
}

private fun ProtoService.toServiceDescriptorProto(packageName: String): ServiceDescriptorProto {
    val builder = ServiceDescriptorProto.newBuilder().setName(name)
    for (rpc in rpcs) {
        builder.addMethod(
            MethodDescriptorProto.newBuilder()
                .setName(rpc.name)
                .setInputType(resolveTypeName(rpc.inputType, packageName))
                .setOutputType(resolveTypeName(rpc.outputType, packageName))
                .setClientStreaming(rpc.clientStreaming)
                .setServerStreaming(rpc.serverStreaming)
        )
    }
    return builder.build()
}

private fun resolveTypeName(typeName: String, packageName: String): String {
    // Already fully qualified
    if (typeName.startsWith(".")) return typeName
    // Scalar types don't need resolution
    if (typeName in scalarTypeMap) return typeName
    // Qualify with package
    return if (packageName.isNotEmpty()) ".$packageName.$typeName" else ".$typeName"
}
