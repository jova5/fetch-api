package ba.fluxor.fetchapi.feature.tabs.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.component.CompactInput
import ba.fluxor.fetchapi.component.SquareButton
import ba.fluxor.fetchapi.feature.tabs.viewmodel.TabBuffer
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.save
import org.jetbrains.compose.resources.stringResource

@Composable
fun FolderTabEditor(
  buffer: TabBuffer.Folder,
  isDirty: Boolean,
  onChange: (TabBuffer) -> Unit,
  onSave: () -> Unit,
) {
  Column(modifier = Modifier.fillMaxSize()
    .padding(16.dp)) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      CompactInput(
        value = buffer.name,
        onValueChange = { onChange(buffer.copy(name = it)) }
      )
      Spacer(Modifier.weight(1f))
      SquareButton(
        text = stringResource(Res.string.save),
        onClick = onSave,
        enabled = isDirty,
        modifier = Modifier
          .padding(horizontal = 16.dp, vertical = 6.dp)
      )
    }
  }
}
