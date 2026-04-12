package ba.fluxor.fetchapi.feature.request.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ba.fluxor.fetchapi.feature.request.data.Request
import ba.fluxor.fetchapi.network.http.HttpMethod
import fetchapi.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun RequestDialog(
  editing: Request?,
  error: String?,
  onSave: (name: String, method: String, url: String) -> Unit,
  onDismiss: () -> Unit,
) {
  var nameInput by remember(editing?.id) { mutableStateOf(editing?.name ?: "") }
  var method by remember(editing?.id) { mutableStateOf(editing?.method ?: HttpMethod.GET.name) }
  var url by remember(editing?.id) { mutableStateOf(editing?.url ?: "") }
  var methodExpanded by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(modifier = Modifier.width(480.dp)) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = if (editing == null) stringResource(Res.string.new_request) else stringResource(Res.string.edit_request, editing.id.toString()),
          style = MaterialTheme.typography.titleMedium,
        )

        OutlinedTextField(
          value = nameInput,
          onValueChange = { nameInput = it },
          label = { Text(stringResource(Res.string.name)) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )

        Box {
          OutlinedTextField(
            value = method,
            onValueChange = {},
            label = { Text(stringResource(Res.string.method)) },
            readOnly = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          DropdownMenu(expanded = methodExpanded, onDismissRequest = { methodExpanded = false }) {
            HttpMethod.entries.forEach { m ->
              DropdownMenuItem(
                text = { Text(m.name) },
                onClick = { method = m.name; methodExpanded = false },
              )
            }
          }
          Surface(
            modifier = Modifier.matchParentSize(),
            color = Color.Transparent,
            onClick = { methodExpanded = true },
            content = {},
          )
        }

        OutlinedTextField(
          value = url,
          onValueChange = { url = it },
          label = { Text(stringResource(Res.string.url)) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )

        error?.let {
          Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(onClick = { onSave(nameInput, method, url) }) {
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
