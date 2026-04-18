package ba.fluxor.fetchapi.feature.tabs.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.feature.tabs.viewmodel.TabBuffer
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.name
import fetchapi.composeapp.generated.resources.save
import org.jetbrains.compose.resources.stringResource

@Composable
fun FolderTabEditor(
  buffer: TabBuffer.Folder,
  isDirty: Boolean,
  onChange: (TabBuffer) -> Unit,
  onSave: () -> Unit,
) {
  Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
      Spacer(Modifier.weight(1f))
      Button(onClick = onSave, enabled = isDirty) {
        Text(stringResource(Res.string.save))
      }
    }
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
      value = buffer.name,
      onValueChange = { onChange(buffer.copy(name = it)) },
      label = { Text(stringResource(Res.string.name)) },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}
