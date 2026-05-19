package ba.fluxor.fetchapi.feature.request.data

import kotlinx.serialization.Serializable

@Serializable
data class KeyValueEntry(
  val key: String = "",
  val value: String = "",
  val description: String = "",
  val enabled: Boolean = true,
  val readOnly: Boolean = false,
)
