package ba.fluxor.fetchapi.feature.project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ba.fluxor.fetchapi.feature.project.data.Project
import ba.fluxor.fetchapi.feature.project.viewmodel.ProjectViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProjectScreen(vm: ProjectViewModel = koinViewModel()) {
  val state by vm.state.collectAsStateWithLifecycle()

  var nameInput by remember { mutableStateOf("") }

  LaunchedEffect(state.selected?.id) {
    nameInput = state.selected?.name ?: ""
  }

  Surface(modifier = Modifier.fillMaxSize()) {
    Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
      // Left: list
      Column(modifier = Modifier.width(260.dp).fillMaxHeight()) {
        Text("Projects", style = MaterialTheme.typography.titleMedium)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        LazyColumn(modifier = Modifier.fillMaxSize()) {
          items(state.projects, key = { it.id ?: -1L }) { project ->
            ProjectRow(
              project = project,
              selected = project.id == state.selected?.id,
              onClick = { project.id?.let(vm::selectById) },
            )
          }
        }
      }

      VerticalDivider(modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp))

      // Right: editor
      Column(
        modifier = Modifier.fillMaxSize().padding(start = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          if (state.selected == null) "New project" else "Edit project #${state.selected?.id}",
          style = MaterialTheme.typography.titleMedium,
        )

        OutlinedTextField(
          value = nameInput,
          onValueChange = { nameInput = it },
          label = { Text("Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            onClick = {
              val selectedId = state.selected?.id
              if (selectedId == null) vm.create(nameInput)
              else vm.update(selectedId, nameInput)
            },
          ) { Text("Save") }

          OutlinedButton(
            onClick = {
              vm.clearSelection()
              nameInput = ""
            },
          ) { Text("New") }

          state.selected?.id?.let { id ->
            OutlinedButton(onClick = { vm.delete(id) }) { Text("Delete") }
          }
        }

        state.error?.let { err ->
          Text(
            text = err,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
          )
        }
      }
    }
  }
}

@Composable
private fun ProjectRow(
  project: Project,
  selected: Boolean,
  onClick: () -> Unit,
) {
  val bg = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(bg)
      .clickable(onClick = onClick)
      .padding(horizontal = 8.dp, vertical = 10.dp),
    contentAlignment = Alignment.CenterStart,
  ) {
    Text(
      text = "${project.id ?: "-"}  ·  ${project.name}",
      style = MaterialTheme.typography.bodyLarge,
    )
  }
}
