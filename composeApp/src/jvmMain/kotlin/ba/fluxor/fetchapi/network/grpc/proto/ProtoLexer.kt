package ba.fluxor.fetchapi.network.grpc.proto

object ProtoLexer {

    fun tokenize(source: String): List<ProtoToken> {
        val tokens = mutableListOf<ProtoToken>()
        var pos = 0
        var line = 1
        var col = 1

        fun peek(): Char? = source.getOrNull(pos)
        fun advance(): Char {
            val ch = source[pos++]
            if (ch == '\n') { line++; col = 1 } else col++
            return ch
        }

        fun error(msg: String): Nothing = throw ProtoParseException(msg, line, col)

        while (pos < source.length) {
            val startLine = line
            val startCol = col
            val ch = peek()!!

            // Whitespace
            if (ch.isWhitespace()) {
                advance()
                continue
            }

            // Single-line comment
            if (ch == '/' && source.getOrNull(pos + 1) == '/') {
                while (pos < source.length && peek() != '\n') advance()
                continue
            }

            // Block comment
            if (ch == '/' && source.getOrNull(pos + 1) == '*') {
                advance(); advance() // consume /*
                while (pos < source.length) {
                    if (peek() == '*' && source.getOrNull(pos + 1) == '/') {
                        advance(); advance() // consume */
                        break
                    }
                    advance()
                }
                continue
            }

            // String literal
            if (ch == '"' || ch == '\'') {
                val quote = advance()
                val sb = StringBuilder()
                while (pos < source.length && peek() != quote) {
                    val c = advance()
                    if (c == '\\') {
                        if (pos >= source.length) error("Unterminated escape sequence")
                        when (val esc = advance()) {
                            'n' -> sb.append('\n')
                            't' -> sb.append('\t')
                            'r' -> sb.append('\r')
                            '\\' -> sb.append('\\')
                            '\'' -> sb.append('\'')
                            '"' -> sb.append('"')
                            else -> { sb.append('\\'); sb.append(esc) }
                        }
                    } else {
                        sb.append(c)
                    }
                }
                if (pos >= source.length) error("Unterminated string literal")
                advance() // closing quote
                tokens += ProtoToken.StringLiteral(sb.toString(), startLine, startCol)
                continue
            }

            // Integer literal (including negative)
            if (ch.isDigit() || (ch == '-' && source.getOrNull(pos + 1)?.isDigit() == true)) {
                val sb = StringBuilder()
                if (ch == '-') sb.append(advance())
                while (pos < source.length && peek()!!.isDigit()) sb.append(advance())
                tokens += ProtoToken.IntLiteral(sb.toString().toInt(), startLine, startCol)
                continue
            }

            // Identifier
            if (ch.isLetter() || ch == '_') {
                val sb = StringBuilder()
                while (pos < source.length && (peek()!!.isLetterOrDigit() || peek() == '_')) {
                    sb.append(advance())
                }
                tokens += ProtoToken.Identifier(sb.toString(), startLine, startCol)
                continue
            }

            // Symbols
            if (ch in "{}=;(),<>.") {
                advance()
                tokens += ProtoToken.Symbol(ch, startLine, startCol)
                continue
            }

            error("Unexpected character '$ch'")
        }

        tokens += ProtoToken.Eof(line, col)
        return tokens
    }
}
