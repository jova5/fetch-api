package ba.fluxor.fetchapi.feature.tabs.ui.request

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ba.fluxor.fetchapi.component.KeyValueDescTable
import ba.fluxor.fetchapi.feature.request.data.KeyValueEntry
import ba.fluxor.fetchapi.feature.tabs.viewmodel.TabBuffer
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.headers
import org.jetbrains.compose.resources.stringResource

@Composable
fun HeadersSection(
  buffer: TabBuffer.Request,
  resolvedAuthHeaders: Map<String, String>,
  autoHidden: Boolean,
  onCustomHeadersChange: (List<KeyValueEntry>) -> Unit,
  onReadOnlyToggle: (String, Boolean) -> Unit,
  onToggleAutoHidden: () -> Unit,
) {
  val readOnlyRows = computeAutoHeaders(buffer, resolvedAuthHeaders)
  val customRows = buffer.headers
  val overriddenKeys = customRows
    .filter { it.enabled && it.key.isNotBlank() }
    .map { it.key.lowercase() }
    .toSet()
  val unified = readOnlyRows + customRows

  Column(modifier = Modifier.fillMaxWidth()) {
    KeyValueDescTable(
      rows = unified,
      onChange = onCustomHeadersChange,
      modifier = Modifier.fillMaxWidth(),
      overriddenKeys = overriddenKeys,
      onReadOnlyToggle = onReadOnlyToggle,
      autoHidden = autoHidden,
      onToggleAutoHidden = onToggleAutoHidden,
      keyPlaceholder = stringResource(Res.string.headers),
    )
  }
}
