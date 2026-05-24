package ba.fluxor.fetchapi.feature.tabs.ui.request.raw.format

import kotlinx.serialization.json.Json

object JsonFormatter : RawFormatter {

  private val pretty = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
  }

  override fun format(text: String): Result<String> {

    if (text.isBlank()) return Result.success(text)

    return runCatching {
      val element = pretty.parseToJsonElement(text)
      pretty.encodeToString(element)
    }
  }

  override fun validate(text: String): Result<Unit> {
    if (text.isBlank()) return Result.success(Unit)
    return runCatching { pretty.parseToJsonElement(text) }.map { }
  }
}
