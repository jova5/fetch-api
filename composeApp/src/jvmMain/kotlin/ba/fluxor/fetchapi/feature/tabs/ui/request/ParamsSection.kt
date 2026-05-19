package ba.fluxor.fetchapi.feature.tabs.ui.request

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ba.fluxor.fetchapi.component.KeyValueDescTable
import ba.fluxor.fetchapi.feature.request.data.KeyValueEntry
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.params
import org.jetbrains.compose.resources.stringResource

@Composable
fun ParamsSection(
  params: List<KeyValueEntry>,
  onChange: (List<KeyValueEntry>) -> Unit,
) {
  KeyValueDescTable(
    rows = params,
    onChange = onChange,
    modifier = Modifier.fillMaxWidth(),
    keyPlaceholder = stringResource(Res.string.params),
    showHideButton = false
  )
}
