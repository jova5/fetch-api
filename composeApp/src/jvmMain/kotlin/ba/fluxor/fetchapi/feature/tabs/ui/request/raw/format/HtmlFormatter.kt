package ba.fluxor.fetchapi.feature.tabs.ui.request.raw.format

import org.jsoup.Jsoup
import org.jsoup.nodes.Entities

object HtmlFormatter : RawFormatter {

  override fun format(text: String): Result<String> {

    if (text.isBlank()) return Result.success(text)

    return runCatching {
      val document = Jsoup.parse(text)
      document.outputSettings()
        .prettyPrint(true)
        .indentAmount(2)
        .escapeMode(Entities.EscapeMode.xhtml)
      document.outerHtml()
    }
  }

  override fun validate(text: String): Result<Unit> {
    if (text.isBlank()) return Result.success(Unit)
    return runCatching { Jsoup.parse(text) }.map { }
  }
}
