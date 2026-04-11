package ba.fluxor.fetchapi.feature.project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ba.fluxor.fetchapi.feature.project.data.Project
import ba.fluxor.fetchapi.feature.project.viewmodel.ProjectViewModel
import fetchapi.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProjectManagerModal(
  onDismiss: () -> Unit,
  projectVm: ProjectViewModel = koinViewModel(),
) {
  val state by projectVm.state.collectAsStateWithLifecycle()
  var nameInput by remember { mutableStateOf("") }

  LaunchedEffect(state.selected?.id) {
    nameInput = state.selected?.name ?: ""
  }

  Dialog(
    onDismissRequest = {
      projectVm.clearSelection()
      onDismiss()
    },
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(modifier = Modifier.size(720.dp, 480.dp)) {
      Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(modifier = Modifier.width(260.dp).fillMaxHeight()) {
          Text(stringResource(Res.string.projects), style = MaterialTheme.typography.titleMedium)
          HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
          LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.projects, key = { it.id ?: -1L }) { project ->
              ProjectRow(
                project = project,
                selected = project.id == state.selected?.id,
                onClick = { project.id?.let(projectVm::selectById) },
              )
            }
          }
        }

        VerticalDivider(modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp))

        Column(
          modifier = Modifier.fillMaxSize().padding(start = 16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Text(
            text = if (state.selected == null) {
              stringResource(Res.string.new_project)
            } else {
              stringResource(Res.string.edit_project, state.selected?.id.toString())
            },
            style = MaterialTheme.typography.titleMedium,
          )

          OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text(stringResource(Res.string.name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              onClick = {
                val selectedId = state.selected?.id
                if (selectedId == null) projectVm.create(nameInput)
                else projectVm.update(selectedId, nameInput)
              },
            ) { Text(stringResource(Res.string.save)) }

            OutlinedButton(
              onClick = {
                projectVm.clearSelection()
                nameInput = ""
              },
            ) { Text(stringResource(Res.string.new)) }

            state.selected?.id?.let { id ->
              OutlinedButton(onClick = { projectVm.delete(id) }) {
                Text(stringResource(Res.string.delete))
              }
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
