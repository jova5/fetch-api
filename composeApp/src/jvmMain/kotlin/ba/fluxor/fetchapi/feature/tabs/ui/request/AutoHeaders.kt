package ba.fluxor.fetchapi.feature.tabs.ui.request

import ba.fluxor.fetchapi.feature.request.data.BodyConfig
import ba.fluxor.fetchapi.feature.request.data.KeyValueEntry
import ba.fluxor.fetchapi.feature.tabs.viewmodel.TabBuffer

fun computeAutoHeaders(buffer: TabBuffer.Request): List<KeyValueEntry> {
  val userKeys = buffer.headers
    .filter { it.enabled && it.key.isNotBlank() }
    .map { it.key.lowercase() }
    .toSet()

  val auto = buildList {
    when (val b = buffer.body) {
      BodyConfig.None -> {}
      is BodyConfig.Raw -> add(KeyValueEntry(key = "Content-Type", value = b.language.mime, description = "auto"))
      is BodyConfig.FormData -> add(KeyValueEntry(key = "Content-Type", value = "multipart/form-data", description = "auto"))
      is BodyConfig.UrlEncoded -> add(KeyValueEntry(key = "Content-Type", value = "application/x-www-form-urlencoded", description = "auto"))
      is BodyConfig.Binary -> add(KeyValueEntry(key = "Content-Type", value = "application/octet-stream", description = "auto"))
    }
    add(KeyValueEntry(key = "User-Agent", value = "FetchAPI/1.0", description = "auto"))
    add(KeyValueEntry(key = "Accept", value = "*/*", description = "auto"))
    add(KeyValueEntry(key = "Accept-Encoding", value = "gzip, deflate, br", description = "auto"))
    add(KeyValueEntry(key = "Connection", value = "keep-alive", description = "auto"))
  }

  return auto.filter { it.key.lowercase() !in userKeys }
}
