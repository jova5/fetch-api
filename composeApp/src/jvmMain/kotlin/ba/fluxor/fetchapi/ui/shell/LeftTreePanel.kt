package ba.fluxor.fetchapi.ui.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ba.fluxor.fetchapi.ui.shell.viewmodel.AppShellViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LeftTreePanel(shellVm: AppShellViewModel = koinViewModel()) {
  val state by shellVm.state.collectAsStateWithLifecycle()
  var query by remember { mutableStateOf("") }

  Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        placeholder = { Text("Search") },
        singleLine = true,
        modifier = Modifier.weight(1f),
      )
      IconButton(onClick = { /* TODO: add sub-project */ }) {
        Icon(Icons.Default.Add, contentDescription = "Add sub-project")
      }
    }

    Box(modifier = Modifier.fillMaxSize().padding(top = 12.dp)) {
      val name = state.activeProjectName
      Text(
        text = if (name == null) {
          "No sub-projects yet"
        } else {
          "$name — sub-projects coming soon"
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
