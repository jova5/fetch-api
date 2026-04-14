package ba.fluxor.fetchapi.ui.shell

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ba.fluxor.fetchapi.feature.folder.ui.FolderDialog
import ba.fluxor.fetchapi.feature.folder.viewmodel.FolderViewModel
import ba.fluxor.fetchapi.feature.project.viewmodel.ProjectViewModel
import ba.fluxor.fetchapi.feature.project_tree.ui.ProjectTree
import ba.fluxor.fetchapi.feature.project_tree.viewmodel.ProjectTreeViewModel
import ba.fluxor.fetchapi.feature.request.ui.RequestDialog
import ba.fluxor.fetchapi.feature.request.viewmodel.RequestViewModel
import ba.fluxor.fetchapi.feature.sub_project.ui.SubProjectDialog
import ba.fluxor.fetchapi.feature.sub_project.viewmodel.SubProjectViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LeftTreePanel(
  treeVm: ProjectTreeViewModel = koinViewModel(),
  projectVm: ProjectViewModel = koinViewModel(),
  subProjectVm: SubProjectViewModel = koinViewModel(),
  folderVm: FolderViewModel = koinViewModel(),
  requestVm: RequestViewModel = koinViewModel()
) {
  val treeState by treeVm.state.collectAsStateWithLifecycle()
  val projectState by projectVm.state.collectAsStateWithLifecycle()
  val subProjectState by subProjectVm.state.collectAsStateWithLifecycle()
  val folderState by folderVm.state.collectAsStateWithLifecycle()
  val requestState by requestVm.state.collectAsStateWithLifecycle()
  var query by remember { mutableStateOf("") }

  LaunchedEffect(projectState.active) {
    val projectId = projectState.active?.id
    if (projectId != null) {
      treeVm.loadTree(projectId)
    } else {
      treeVm.clearTree()
    }
  }

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
      IconButton(
        onClick = { subProjectVm.showNewSubProjectDialog() },
        enabled = projectState.active != null,
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add sub-project")
      }
    }

    Box(modifier = Modifier.fillMaxSize().padding(top = 12.dp)) {
      if (projectState.active == null) {
        Text(
          text = "Select a project",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      } else {
        ProjectTree(
          nodes = treeState.subProjectNodes,
          query = query,
          treeVm = treeVm,
          subProjectVm = subProjectVm,
          folderVm = folderVm,
          requestVm = requestVm,
        )
      }
    }
  }

  if (subProjectState.showSubProjectDialog) {
    SubProjectDialog(
      editing = subProjectState.editingSubProject,
      error = subProjectState.error,
      onSave = { name, authType, authConfig ->
        val editing = subProjectState.editingSubProject
        if (editing?.id != null) {
          subProjectVm.updateSubProject(editing.id, name, authType, authConfig)
        } else {
          subProjectVm.createSubProject(name, treeState.projectId)
        }
      },
      onDismiss = subProjectVm::dismissDialogs,
    )
  }

  if (folderState.showFolderDialog) {
    FolderDialog(
      editing = folderState.editingFolder,
      error = folderState.error,
      onSave = { name ->
        val editing = folderState.editingFolder
        if (editing?.id != null) {
          folderVm.updateFolder(editing.id, name)
        } else {
          folderState.dialogParentSubProjectId?.let { folderVm.createFolder(it, name) }
        }
      },
      onDismiss = folderVm::dismissDialogs,
    )
  }

  if (requestState.showRequestDialog) {
    RequestDialog(
      editing = requestState.editingRequest,
      error = requestState.error,
      onSave = { name, method, url ->
        val editing = requestState.editingRequest
        if (editing?.id != null) {
          requestVm.updateRequest(editing.id, editing.folderId, name, method, url, editing.headers,
            editing.body)
        } else {
          val spId = requestState.dialogParentSubProjectId
          if (spId != null) {
            requestVm.createRequest(spId, requestState.dialogParentFolderId, name, method, url)
          }
        }
      },
      onDismiss = requestVm::dismissDialogs,
    )
  }
}
