package ba.fluxor.fetchapi.feature.request.data

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

object RequestCodecs {

  private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    classDiscriminator = "type"
  }

  fun encodeKeyValues(rows: List<KeyValueEntry>): String? {
    if (rows.isEmpty()) return null
    return json.encodeToString(ListSerializer(KeyValueEntry.serializer()), rows)
  }

  fun decodeKeyValues(raw: String?): List<KeyValueEntry> {
    if (raw.isNullOrBlank()) return emptyList()
    return runCatching { json.decodeFromString(ListSerializer(KeyValueEntry.serializer()), raw) }
      .getOrDefault(emptyList())
  }

  fun encodeBody(body: BodyConfig): String? {
    if (body is BodyConfig.None) return null
    return json.encodeToString(BodyConfig.serializer(), body)
  }

  fun decodeBody(raw: String?): BodyConfig {
    if (raw.isNullOrBlank()) return BodyConfig.None
    return runCatching { json.decodeFromString(BodyConfig.serializer(), raw) }
      .getOrDefault(BodyConfig.None)
  }

  fun encodeStringSet(set: Set<String>): String? {
    if (set.isEmpty()) return null
    return json.encodeToString(ListSerializer(String.serializer()), set.toList())
  }

  fun decodeStringSet(raw: String?): Set<String> {
    if (raw.isNullOrBlank()) return emptySet()
    return runCatching { json.decodeFromString(ListSerializer(String.serializer()), raw).toSet() }
      .getOrDefault(emptySet())
  }
}
