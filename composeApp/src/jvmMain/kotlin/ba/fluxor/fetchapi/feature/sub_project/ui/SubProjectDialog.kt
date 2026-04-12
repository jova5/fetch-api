package ba.fluxor.fetchapi.feature.sub_project.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ba.fluxor.fetchapi.feature.sub_project.data.SubProject
import fetchapi.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun SubProjectDialog(
  editing: SubProject?,
  error: String?,
  onSave: (name: String, authType: String, authConfig: String?) -> Unit,
  onDismiss: () -> Unit,
) {
  var nameInput by remember(editing?.id) { mutableStateOf(editing?.name ?: "") }
  var authType by remember(editing?.id) { mutableStateOf(editing?.authType ?: "NONE") }
  var authConfig by remember(editing?.id) { mutableStateOf(editing?.authConfig ?: "") }

  Dialog(onDismissRequest = onDismiss) {
    Surface(modifier = Modifier.width(400.dp)) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = if (editing == null) stringResource(Res.string.new_sub_project) else stringResource(Res.string.edit_sub_project, editing.id.toString()),
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
          Button(onClick = { onSave(nameInput, authType, authConfig.ifBlank { null }) }) {
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
