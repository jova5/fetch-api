package ba.fluxor.fetchapi.feature.tabs.ui.request.raw.format

import ba.fluxor.fetchapi.feature.request.data.RawLanguage

fun RawLanguage.formatter(): RawFormatter? = when (this) {
  RawLanguage.JSON -> JsonFormatter
  RawLanguage.XML -> XmlFormatter
  RawLanguage.HTML -> HtmlFormatter
  RawLanguage.TEXT, RawLanguage.JAVASCRIPT -> null
}
