package ba.fluxor.fetchapi.feature.tabs.ui.request

import ba.fluxor.fetchapi.feature.request.data.KeyValueEntry
import ba.fluxor.fetchapi.feature.request.data.RequestHeaderDerivation
import ba.fluxor.fetchapi.feature.tabs.viewmodel.TabBuffer

fun computeAutoHeaders(
  buffer: TabBuffer.Request,
  resolvedAuthHeaders: Map<String, String> = emptyMap(),
): List<KeyValueEntry> {
  val disabled = buffer.excludedAutoHeaders
  return buildList {
    val contentType = RequestHeaderDerivation.contentTypeFor(buffer.body)
    if (contentType.isNotEmpty()) {
      add(KeyValueEntry(
        key = "Content-Type",
        value = contentType,
        description = "auto",
        enabled = "Content-Type" !in disabled,
        readOnly = true,
      ))
    }

    resolvedAuthHeaders.forEach { (k, v) ->
      add(KeyValueEntry(
        key = k,
        value = v,
        description = "auth",
        enabled = k !in disabled,
        readOnly = true,
      ))
    }

    RequestHeaderDerivation.staticAutoHeaders().forEach { (k, v) ->
      add(KeyValueEntry(
        key = k,
        value = v,
        description = "auto",
        enabled = k !in disabled,
        readOnly = true,
      ))
    }
  }
}
