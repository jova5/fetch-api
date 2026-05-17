package ba.fluxor.fetchapi.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

private val jsonPrettyPrinter = Json { prettyPrint = true }

fun formatAndHighlightJson(rawJson: String, isDarkTheme: Boolean): AnnotatedString {
  val prettyJson = try {
    val jsonElement = jsonPrettyPrinter.parseToJsonElement(rawJson)
    jsonPrettyPrinter.encodeToString(JsonElement.serializer(), jsonElement)
  } catch (e: Exception) {
    return AnnotatedString(rawJson)
  }

  val builder = AnnotatedString.Builder(prettyJson)

  val keyColor = if (isDarkTheme) Color(0xFF9CDCFE) else Color(0xFF0033B3)       // Plava
  val stringColor = if (isDarkTheme) Color(0xFFCE9178) else Color(0xFF067D17)    // Crvenkasto-smeđa
  val primitiveColor = if (isDarkTheme) Color(0xFFB5CEA8) else Color(0xFF913100) // Zelena

  val keyRegex = "\"(\\w+)\"\\s*:".toRegex()
  keyRegex.findAll(prettyJson).forEach { match ->
    builder.addStyle(
      style = SpanStyle(color = keyColor), // Svetlo plava za ključeve
      start = match.range.first,
      end = match.range.last + 1
    )
  }

  val stringValueRegex = ":\\s*\"([^\"]*)\"".toRegex()
  stringValueRegex.findAll(prettyJson).forEach { match ->
    val startOfValue = match.value.indexOf('"') + match.range.first
    builder.addStyle(
      style = SpanStyle(color = stringColor),
      start = startOfValue,
      end = match.range.last + 1
    )
  }

  val primitiveRegex = ":\\s*(true|false|null|\\d+(\\.\\d+)?)".toRegex()
  primitiveRegex.findAll(prettyJson).forEach { match ->
    val startOfValue = match.value.indexOfFirst { it != ':' && !it.isWhitespace() } + match.range.first
    builder.addStyle(
      style = SpanStyle(color = primitiveColor),
      start = startOfValue,
      end = match.range.last + 1
    )
  }

  return builder.toAnnotatedString()
}
