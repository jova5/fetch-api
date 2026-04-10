package ba.fluxor.fetchapi.network.grpc.proto

class ProtoParser(private val tokens: List<ProtoToken>) {

    private var cursor = 0

    private fun peek(): ProtoToken = tokens[cursor]

    private fun advance(): ProtoToken = tokens[cursor++]

    private fun isEof(): Boolean = peek() is ProtoToken.Eof

    private fun error(msg: String): Nothing {
        val token = peek()
        throw ProtoParseException(msg, token.line, token.column)
    }

    private fun expectIdentifier(): String {
        val token = advance()
        if (token is ProtoToken.Identifier) return token.value
        throw ProtoParseException("Expected identifier but found ${tokenDesc(token)}", token.line, token.column)
    }

    private fun expectKeyword(keyword: String) {
        val token = advance()
        if (token is ProtoToken.Identifier && token.value == keyword) return
        throw ProtoParseException("Expected '$keyword' but found ${tokenDesc(token)}", token.line, token.column)
    }

    private fun expectSymbol(char: Char) {
        val token = advance()
        if (token is ProtoToken.Symbol && token.char == char) return
        throw ProtoParseException("Expected '$char' but found ${tokenDesc(token)}", token.line, token.column)
    }

    private fun expectString(): String {
        val token = advance()
        if (token is ProtoToken.StringLiteral) return token.value
        throw ProtoParseException("Expected string literal but found ${tokenDesc(token)}", token.line, token.column)
    }

    private fun expectInt(): Int {
        val token = advance()
        if (token is ProtoToken.IntLiteral) return token.value
        throw ProtoParseException("Expected integer but found ${tokenDesc(token)}", token.line, token.column)
    }

    private fun checkIdentifier(value: String): Boolean {
        val token = peek()
        return token is ProtoToken.Identifier && token.value == value
    }

    private fun checkSymbol(char: Char): Boolean {
        val token = peek()
        return token is ProtoToken.Symbol && token.char == char
    }

    private fun tokenDesc(token: ProtoToken): String = when (token) {
        is ProtoToken.Identifier -> "'${token.value}'"
        is ProtoToken.StringLiteral -> "\"${token.value}\""
        is ProtoToken.IntLiteral -> "${token.value}"
        is ProtoToken.Symbol -> "'${token.char}'"
        is ProtoToken.Eof -> "end of file"
    }

    fun parse(): ProtoFile {
        var syntax = "proto3"
        var packageName = ""
        val options = mutableMapOf<String, String>()
        val imports = mutableListOf<String>()
        val messages = mutableListOf<ProtoMessage>()
        val enums = mutableListOf<ProtoEnum>()
        val services = mutableListOf<ProtoService>()

        while (!isEof()) {
            val token = peek()
            if (token !is ProtoToken.Identifier) error("Expected top-level declaration")

            when (token.value) {
                "syntax" -> syntax = parseSyntax()
                "package" -> packageName = parsePackage()
                "option" -> { val (k, v) = parseOption(); options[k] = v }
                "import" -> imports += parseImport()
                "message" -> messages += parseMessage()
                "enum" -> enums += parseEnum()
                "service" -> services += parseService()
                else -> error("Unexpected top-level keyword '${token.value}'")
            }
        }

        return ProtoFile(
            syntax = syntax,
            packageName = packageName,
            options = options,
            imports = imports,
            messages = messages,
            enums = enums,
            services = services,
        )
    }

    private fun parseSyntax(): String {
        expectKeyword("syntax")
        expectSymbol('=')
        val value = expectString()
        expectSymbol(';')
        return value
    }

    private fun parsePackage(): String {
        expectKeyword("package")
        val name = parseDottedName()
        expectSymbol(';')
        return name
    }

    private fun parseImport(): String {
        expectKeyword("import")
        // optional "public" or "weak"
        if (checkIdentifier("public") || checkIdentifier("weak")) advance()
        val path = expectString()
        expectSymbol(';')
        return path
    }

    private fun parseOption(): Pair<String, String> {
        expectKeyword("option")
        val name = parseOptionName()
        expectSymbol('=')
        val value = parseOptionValue()
        expectSymbol(';')
        return name to value
    }

    private fun parseOptionName(): String {
        val sb = StringBuilder()
        // Handle parenthesized custom options: (custom.option)
        if (checkSymbol('(')) {
            advance()
            sb.append('(')
            sb.append(parseDottedName())
            expectSymbol(')')
            sb.append(')')
        } else {
            sb.append(expectIdentifier())
        }
        // Handle sub-field access: option (foo).bar = ...
        while (checkSymbol('.')) {
            advance()
            sb.append('.')
            sb.append(expectIdentifier())
        }
        return sb.toString()
    }

    private fun parseOptionValue(): String = when (val token = peek()) {
        is ProtoToken.StringLiteral -> { advance(); token.value }
        is ProtoToken.IntLiteral -> { advance(); token.value.toString() }
        is ProtoToken.Identifier -> { advance(); token.value }
        else -> error("Expected option value")
    }

    private fun parseMessage(): ProtoMessage {
        expectKeyword("message")
        val name = expectIdentifier()
        expectSymbol('{')

        val fields = mutableListOf<ProtoField>()
        val nestedMessages = mutableListOf<ProtoMessage>()
        val nestedEnums = mutableListOf<ProtoEnum>()
        val oneofs = mutableListOf<ProtoOneof>()

        while (!checkSymbol('}')) {
            if (isEof()) error("Unexpected end of file inside message '$name'")

            val token = peek()
            if (token !is ProtoToken.Identifier) error("Expected field or nested declaration")

            when (token.value) {
                "message" -> nestedMessages += parseMessage()
                "enum" -> nestedEnums += parseEnum()
                "oneof" -> oneofs += parseOneof()
                "map" -> fields += parseMapField()
                "repeated" -> { advance(); fields += parseField(ProtoFieldLabel.REPEATED) }
                "optional" -> { advance(); fields += parseField(ProtoFieldLabel.OPTIONAL) }
                "required" -> { advance(); fields += parseField(ProtoFieldLabel.REQUIRED) }
                "reserved" -> skipReserved()
                "option" -> { parseOption() } // message-level option, discard
                "extensions" -> skipUntilSemicolon()
                else -> fields += parseField(ProtoFieldLabel.OPTIONAL)
            }
        }
        expectSymbol('}')

        return ProtoMessage(
            name = name,
            fields = fields,
            nestedMessages = nestedMessages,
            nestedEnums = nestedEnums,
            oneofs = oneofs,
        )
    }

    private fun parseField(label: ProtoFieldLabel): ProtoField {
        val typeName = parseDottedName()
        val fieldName = expectIdentifier()
        expectSymbol('=')
        val number = expectInt()
        // optional field options [...]
        if (checkSymbol('[')) skipBracketedOptions()
        expectSymbol(';')
        return ProtoField(
            name = fieldName,
            typeName = typeName,
            number = number,
            label = label,
        )
    }

    private fun parseMapField(): ProtoField {
        expectKeyword("map")
        expectSymbol('<')
        val keyType = expectIdentifier()
        expectSymbol(',')
        val valueType = parseDottedName()
        expectSymbol('>')
        val fieldName = expectIdentifier()
        expectSymbol('=')
        val number = expectInt()
        if (checkSymbol('[')) skipBracketedOptions()
        expectSymbol(';')
        return ProtoField(
            name = fieldName,
            typeName = "map<$keyType, $valueType>",
            number = number,
            label = ProtoFieldLabel.MAP,
            mapKeyType = keyType,
            mapValueType = valueType,
        )
    }

    private fun parseOneof(): ProtoOneof {
        expectKeyword("oneof")
        val name = expectIdentifier()
        expectSymbol('{')
        val fields = mutableListOf<ProtoField>()
        while (!checkSymbol('}')) {
            if (isEof()) error("Unexpected end of file inside oneof '$name'")
            if (checkIdentifier("option")) {
                parseOption()
            } else {
                fields += parseField(ProtoFieldLabel.OPTIONAL)
            }
        }
        expectSymbol('}')
        return ProtoOneof(name = name, fields = fields)
    }

    private fun parseEnum(): ProtoEnum {
        expectKeyword("enum")
        val name = expectIdentifier()
        expectSymbol('{')
        val values = mutableListOf<ProtoEnumValue>()
        while (!checkSymbol('}')) {
            if (isEof()) error("Unexpected end of file inside enum '$name'")
            if (checkIdentifier("option")) {
                parseOption()
            } else if (checkIdentifier("reserved")) {
                skipReserved()
            } else {
                val valueName = expectIdentifier()
                expectSymbol('=')
                val number = expectInt()
                if (checkSymbol('[')) skipBracketedOptions()
                expectSymbol(';')
                values += ProtoEnumValue(valueName, number)
            }
        }
        expectSymbol('}')
        return ProtoEnum(name = name, values = values)
    }

    private fun parseService(): ProtoService {
        expectKeyword("service")
        val name = expectIdentifier()
        expectSymbol('{')
        val rpcs = mutableListOf<ProtoRpc>()
        while (!checkSymbol('}')) {
            if (isEof()) error("Unexpected end of file inside service '$name'")
            if (checkIdentifier("option")) {
                parseOption()
            } else if (checkIdentifier("rpc")) {
                rpcs += parseRpc()
            } else {
                error("Expected 'rpc' or 'option' inside service")
            }
        }
        expectSymbol('}')
        return ProtoService(name = name, rpcs = rpcs)
    }

    private fun parseRpc(): ProtoRpc {
        expectKeyword("rpc")
        val name = expectIdentifier()
        expectSymbol('(')

        val clientStreaming = checkIdentifier("stream")
        if (clientStreaming) advance()
        val inputType = parseDottedName()
        expectSymbol(')')

        expectKeyword("returns")
        expectSymbol('(')

        val serverStreaming = checkIdentifier("stream")
        if (serverStreaming) advance()
        val outputType = parseDottedName()
        expectSymbol(')')

        // rpc body or semicolon
        if (checkSymbol('{')) {
            advance()
            while (!checkSymbol('}')) {
                if (isEof()) error("Unexpected end of file inside rpc '$name'")
                if (checkIdentifier("option")) {
                    parseOption()
                } else {
                    skipUntilSemicolon()
                }
            }
            expectSymbol('}')
        } else {
            expectSymbol(';')
        }

        return ProtoRpc(
            name = name,
            inputType = inputType,
            outputType = outputType,
            clientStreaming = clientStreaming,
            serverStreaming = serverStreaming,
        )
    }

    private fun parseDottedName(): String {
        val sb = StringBuilder()
        // optional leading dot for fully qualified names
        if (checkSymbol('.')) {
            advance()
            sb.append('.')
        }
        sb.append(expectIdentifier())
        while (checkSymbol('.')) {
            advance()
            sb.append('.')
            sb.append(expectIdentifier())
        }
        return sb.toString()
    }

    private fun skipBracketedOptions() {
        expectSymbol('[')
        var depth = 1
        while (depth > 0) {
            if (isEof()) error("Unexpected end of file inside field options")
            val token = advance()
            if (token is ProtoToken.Symbol) {
                if (token.char == '[') depth++
                if (token.char == ']') depth--
            }
        }
    }

    private fun skipReserved() {
        advance() // "reserved"
        skipUntilSemicolon()
    }

    private fun skipUntilSemicolon() {
        while (!isEof()) {
            val token = advance()
            if (token is ProtoToken.Symbol && token.char == ';') return
        }
    }

    companion object {
        fun parse(source: String): ProtoFile {
            val tokens = ProtoLexer.tokenize(source)
            return ProtoParser(tokens).parse()
        }
    }
}
