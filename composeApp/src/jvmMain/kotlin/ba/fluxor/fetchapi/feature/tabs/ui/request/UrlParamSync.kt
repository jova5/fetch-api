package ba.fluxor.fetchapi.feature.tabs.ui.request

import ba.fluxor.fetchapi.feature.request.data.KeyValueEntry
import java.net.URLDecoder
import java.net.URLEncoder

object UrlParamSync {

  private data class UrlParts(val base: String, val query: String, val fragment: String)

  private fun split(url: String): UrlParts {
    val hash = url.indexOf('#')
    val noFragment = if (hash >= 0) url.substring(0, hash) else url
    val fragment = if (hash >= 0) url.substring(hash) else ""
    val q = noFragment.indexOf('?')
    val base = if (q >= 0) noFragment.substring(0, q) else noFragment
    val query = if (q >= 0) noFragment.substring(q + 1) else ""
    return UrlParts(base, query, fragment)
  }

  private fun decode(s: String): String =
    runCatching { URLDecoder.decode(s, "UTF-8") }.getOrDefault(s)

  private fun encode(s: String): String = URLEncoder.encode(s, "UTF-8")

  private fun parseQuery(query: String): List<Pair<String, String>> {
    if (query.isBlank()) return emptyList()
    return query.split('&').filter { it.isNotEmpty() }.map { pair ->
      val eq = pair.indexOf('=')
      if (eq >= 0) decode(pair.substring(0, eq)) to decode(pair.substring(eq + 1))
      else decode(pair) to ""
    }
  }

  private fun buildQuery(rows: List<KeyValueEntry>): String =
    rows.filter { it.enabled && it.key.isNotBlank() }
      .joinToString("&") { "${encode(it.key)}=${encode(it.value)}" }

  fun rebuildUrl(url: String, params: List<KeyValueEntry>): String {
    val parts = split(url)
    val q = buildQuery(params)
    return if (q.isBlank()) parts.base + parts.fragment
    else parts.base + "?" + q + parts.fragment
  }

  fun mergeFromUrl(url: String, currentParams: List<KeyValueEntry>): List<KeyValueEntry> {
    val pairs = parseQuery(split(url).query)
    val disabledOrBlank = currentParams.filter { !it.enabled || it.key.isBlank() }

    val fromUrl = pairs.map { (k, v) ->
      val existing = currentParams.firstOrNull { it.enabled && it.key == k }
      KeyValueEntry(
        key = k,
        value = v,
        description = existing?.description.orEmpty(),
        enabled = true,
      )
    }

    return fromUrl + disabledOrBlank
  }
}
