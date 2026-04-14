package ba.fluxor.fetchapi.feature.project.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ba.fluxor.fetchapi.feature.project.viewmodel.ProjectViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProjectDropdown(
  projectVm: ProjectViewModel = koinViewModel()
) {
  val projectState by projectVm.state.collectAsStateWithLifecycle()

  var expanded by remember { mutableStateOf(false) }
  var showManager by remember { mutableStateOf(false) }
  var query by remember { mutableStateOf("") }

  LaunchedEffect(expanded) {
    if (expanded) {
      query = ""
      projectVm.loadAll()
    }
  }

  Box {
    TextButton(onClick = { expanded = true }) {
      Text(projectState.active?.name ?: "Select project")
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
                projectVm.setActiveProject(project)
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
