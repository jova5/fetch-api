package ba.fluxor.fetchapi.feature.tabs.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.feature.tabs.viewmodel.TabBuffer
import ba.fluxor.fetchapi.network.http.HttpMethod
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.body
import fetchapi.composeapp.generated.resources.headers
import fetchapi.composeapp.generated.resources.method
import fetchapi.composeapp.generated.resources.name
import fetchapi.composeapp.generated.resources.save
import fetchapi.composeapp.generated.resources.url
import org.jetbrains.compose.resources.stringResource

@Composable
fun RequestTabEditor(
  buffer: TabBuffer.Request,
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
    Spacer(Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
      MethodDropdown(
        selected = buffer.method,
        onSelect = { onChange(buffer.copy(method = it)) },
        modifier = Modifier.width(140.dp),
      )
      Spacer(Modifier.width(8.dp))
      OutlinedTextField(
        value = buffer.url,
        onValueChange = { onChange(buffer.copy(url = it)) },
        label = { Text(stringResource(Res.string.url)) },
        singleLine = true,
        modifier = Modifier.weight(1f),
      )
    }
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
      value = buffer.headers.orEmpty(),
      onValueChange = { onChange(buffer.copy(headers = it.ifBlank { null })) },
      label = { Text(stringResource(Res.string.headers)) },
      modifier = Modifier.fillMaxWidth().height(120.dp),
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
      value = buffer.body.orEmpty(),
      onValueChange = { onChange(buffer.copy(body = it.ifBlank { null })) },
      label = { Text(stringResource(Res.string.body)) },
      modifier = Modifier.fillMaxWidth().weight(1f),
    )
  }
}

@Composable
private fun MethodDropdown(selected: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
  var expanded by remember { mutableStateOf(false) }
  Box(modifier = modifier) {
    OutlinedTextField(
      value = selected,
      onValueChange = {},
      readOnly = true,
      label = { Text(stringResource(Res.string.method)) },
      modifier = Modifier.fillMaxWidth(),
    )
    Box(modifier = Modifier.matchParentSize()) {
      TextButton(
        onClick = { expanded = true },
        modifier = Modifier.fillMaxSize(),
      ) {}
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      HttpMethod.entries.forEach { m ->
        DropdownMenuItem(
          text = { Text(m.name) },
          onClick = {
            onSelect(m.name)
            expanded = false
          },
        )
      }
    }
  }
}
