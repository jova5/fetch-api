package ba.fluxor.fetchapi.feature.tabs.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.feature.tabs.viewmodel.TabBuffer
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.auth_config
import fetchapi.composeapp.generated.resources.auth_type
import fetchapi.composeapp.generated.resources.name
import fetchapi.composeapp.generated.resources.save
import org.jetbrains.compose.resources.stringResource

private val AUTH_TYPES = listOf("NONE", "BASIC", "BEARER", "API_KEY")

@Composable
fun SubProjectTabEditor(
  buffer: TabBuffer.SubProject,
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
    AuthTypeDropdown(
      selected = buffer.authType,
      onSelect = { onChange(buffer.copy(authType = it)) },
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
      value = buffer.authConfig.orEmpty(),
      onValueChange = { onChange(buffer.copy(authConfig = it.ifBlank { null })) },
      label = { Text(stringResource(Res.string.auth_config)) },
      modifier = Modifier.fillMaxWidth().height(160.dp),
    )
  }
}

@Composable
private fun AuthTypeDropdown(selected: String, onSelect: (String) -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  Box {
    OutlinedTextField(
      value = selected,
      onValueChange = {},
      readOnly = true,
      label = { Text(stringResource(Res.string.auth_type)) },
      modifier = Modifier.fillMaxWidth(),
    )
    Box(
      modifier = Modifier.matchParentSize().padding(top = 8.dp),
    ) {
      TextButton(
        onClick = { expanded = true },
        modifier = Modifier.fillMaxSize(),
      ) {}
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      AUTH_TYPES.forEach { type ->
        DropdownMenuItem(
          text = { Text(type) },
          onClick = {
            onSelect(type)
            expanded = false
          },
        )
      }
    }
  }
}
