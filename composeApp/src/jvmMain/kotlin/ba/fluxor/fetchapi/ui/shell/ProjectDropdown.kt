package ba.fluxor.fetchapi.ui.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ba.fluxor.fetchapi.feature.project.viewmodel.ProjectViewModel
import ba.fluxor.fetchapi.ui.shell.viewmodel.AppShellViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProjectDropdown(
  projectVm: ProjectViewModel = koinViewModel(),
  shellVm: AppShellViewModel = koinViewModel(),
) {
  val projectState by projectVm.state.collectAsStateWithLifecycle()
  val shellState by shellVm.state.collectAsStateWithLifecycle()

  var expanded by remember { mutableStateOf(false) }
  var showManager by remember { mutableStateOf(false) }
  var query by remember { mutableStateOf("") }

  LaunchedEffect(expanded) {
    if (expanded) {
      query = ""
      projectVm.loadAll()
    }
  }

  // Keep the active project name in sync if projects change.
  LaunchedEffect(projectState.projects, shellState.activeProjectId) {
    val id = shellState.activeProjectId ?: return@LaunchedEffect
    val current = projectState.projects.firstOrNull { it.id == id }
    if (current == null) {
      shellVm.onProjectDeleted(id)
    } else if (current.name != shellState.activeProjectName) {
      shellVm.onProjectRenamed(id, current.name)
    }
  }

  Box {
    TextButton(onClick = { expanded = true }) {
      Text(shellState.activeProjectName ?: "Select project")
      Icon(Icons.Default.ArrowDropDown, contentDescription = null)
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier.width(320.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        OutlinedTextField(
          value = query,
          onValueChange = { query = it },
          placeholder = { Text("Search") },
          singleLine = true,
          modifier = Modifier.weight(1f),
        )
        IconButton(onClick = {
          showManager = true
          expanded = false
        }) {
          Icon(Icons.Default.Edit, contentDescription = "Manage projects")
        }
      }

      val filtered = remember(projectState.projects, query) {
        if (query.isBlank()) projectState.projects
        else projectState.projects.filter { it.name.contains(query, ignoreCase = true) }
      }

      if (filtered.isEmpty()) {
        DropdownMenuItem(
          text = { Text("No projects", color = MaterialTheme.colorScheme.onSurfaceVariant) },
          onClick = {},
          enabled = false,
        )
      } else {
        Column(
          modifier = Modifier
            .heightIn(max = 400.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        ) {
          filtered.forEach { project ->
            DropdownMenuItem(
              text = { Text(project.name) },
              onClick = {
                shellVm.setActiveProject(project)
                expanded = false
              },
            )
          }
        }
      }
    }
  }

  if (showManager) {
    ProjectManagerModal(
      onDismiss = { showManager = false },
      projectVm = projectVm,
    )
  }
}
