package ba.fluxor.fetchapi.feature.tabs.ui.request.raw.format

interface RawFormatter {
  fun format(text: String): Result<String>
  fun validate(text: String): Result<Unit>
}
