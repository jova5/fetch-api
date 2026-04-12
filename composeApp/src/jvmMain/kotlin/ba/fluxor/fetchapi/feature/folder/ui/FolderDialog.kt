package ba.fluxor.fetchapi.feature.folder.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ba.fluxor.fetchapi.feature.folder.data.Folder
import fetchapi.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun FolderDialog(
  editing: Folder?,
  error: String?,
  onSave: (name: String) -> Unit,
  onDismiss: () -> Unit,
) {
  var nameInput by remember(editing?.id) { mutableStateOf(editing?.name ?: "") }

  Dialog(onDismissRequest = onDismiss) {
    Surface(modifier = Modifier.width(400.dp)) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = if (editing == null) stringResource(Res.string.new_folder) else stringResource(Res.string.edit_folder, editing.id.toString()),
          style = MaterialTheme.typography.titleMedium,
        )

        OutlinedTextField(
          value = nameInput,
          onValueChange = { nameInput = it },
          label = { Text(stringResource(Res.string.name)) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )

        error?.let {
          Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(onClick = { onSave(nameInput) }) {
            Text(stringResource(Res.string.save))
          }
          OutlinedButton(onClick = onDismiss) {
            Text(stringResource(Res.string.close))
          }
        }
      }
    }
  }
}
