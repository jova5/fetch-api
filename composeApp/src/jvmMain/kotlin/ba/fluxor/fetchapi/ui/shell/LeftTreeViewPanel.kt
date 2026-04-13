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
import ba.fluxor.fetchapi.feature.project_tree.ui.ProjectTree
import ba.fluxor.fetchapi.feature.project_tree.viewmodel.ProjectTreeViewModel
import ba.fluxor.fetchapi.feature.request.ui.RequestDialog
import ba.fluxor.fetchapi.feature.sub_project.ui.SubProjectDialog
import ba.fluxor.fetchapi.ui.shell.viewmodel.AppShellViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LeftTreePanel(
  shellVm: AppShellViewModel = koinViewModel(),
  treeVm: ProjectTreeViewModel = koinViewModel(),
  folderVm: FolderViewModel = koinViewModel(),
) {
  val shellState by shellVm.state.collectAsStateWithLifecycle()
  val treeState by treeVm.state.collectAsStateWithLifecycle()
  val folderState by folderVm.state.collectAsStateWithLifecycle()
  var query by remember { mutableStateOf("") }

  LaunchedEffect(shellState.activeProjectId) {
    val projectId = shellState.activeProjectId
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
        onClick = { treeVm.showNewSubProjectDialog() },
        enabled = shellState.activeProjectId != null,
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add sub-project")
      }
    }

    Box(modifier = Modifier.fillMaxSize().padding(top = 12.dp)) {
      if (shellState.activeProjectId == null) {
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
          folderVm = folderVm,
        )
      }
    }
  }

  // Dialogs
  if (treeState.showSubProjectDialog) {
    SubProjectDialog(
      editing = treeState.editingSubProject,
      error = treeState.error,
      onSave = { name, authType, authConfig ->
        val editing = treeState.editingSubProject
        if (editing?.id != null) {
          treeVm.updateSubProject(editing.id, name, authType, authConfig)
        } else {
          treeVm.createSubProject(name)
        }
      },
      onDismiss = treeVm::dismissDialogs,
    )
  }

  if (folderState.showFolderDialog) {
    FolderDialog(
      editing = folderState.editingFolder,
      error = folderState.error,
      onSave = { name ->
        val editing = folderState.editingFolder
        if (editing?.id != null) {
          folderVm.updateFolder(editing.id, editing.subProjectId, name)
        } else {
          folderState.dialogParentSubProjectId?.let { folderVm.createFolder(it, name) }
        }
      },
      onDismiss = folderVm::dismissDialogs,
    )
  }

  if (treeState.showRequestDialog) {
    RequestDialog(
      editing = treeState.editingRequest,
      error = treeState.error,
      onSave = { name, method, url ->
        val editing = treeState.editingRequest
        if (editing?.id != null) {
          treeVm.updateRequest(editing.id, editing.folderId, name, method, url, editing.headers, editing.body)
        } else {
          val spId = treeState.dialogParentSubProjectId
          if (spId != null) {
            treeVm.createRequest(spId, treeState.dialogParentFolderId, name, method, url)
          }
        }
      },
      onDismiss = treeVm::dismissDialogs,
    )
  }
}
