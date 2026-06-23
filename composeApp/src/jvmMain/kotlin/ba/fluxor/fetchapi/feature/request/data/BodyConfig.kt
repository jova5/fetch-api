package ba.fluxor.fetchapi.feature.request.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class RawLanguage {
  JSON, XML, TEXT, HTML, JAVASCRIPT;

  val mime: String
    get() = when (this) {
      JSON -> "application/json"
      XML -> "application/xml"
      TEXT -> "text/plain"
      HTML -> "text/html"
      JAVASCRIPT -> "application/javascript"
    }
}

@Serializable
data class FormDataEntry(
  val key: String = "",
  val value: String = "",
  val description: String = "",
  val enabled: Boolean = true,
  val isFile: Boolean = false,
  val filePaths: List<String> = emptyList(),
)

@Serializable
sealed class BodyConfig {

  @Serializable
  @SerialName("NONE")
  data object None : BodyConfig()

  @Serializable
  @SerialName("RAW")
  data class Raw(
    val language: RawLanguage = RawLanguage.JSON,
    val content: String = "",
  ) : BodyConfig()

  @Serializable
  @SerialName("FORM_DATA")
  data class FormData(
    val fields: List<FormDataEntry> = emptyList(),
  ) : BodyConfig()

  @Serializable
  @SerialName("URL_ENCODED")
  data class UrlEncoded(
    val fields: List<KeyValueEntry> = emptyList(),
  ) : BodyConfig()

  @Serializable
  @SerialName("BINARY")
  data class Binary(
    val filePath: String? = null,
  ) : BodyConfig()
}
