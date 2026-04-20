package ba.fluxor.fetchapi.feature.project_tree.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ba.fluxor.fetchapi.component.CompactInput
import ba.fluxor.fetchapi.component.SquareIconButton
import ba.fluxor.fetchapi.feature.folder.viewmodel.FolderViewModel
import ba.fluxor.fetchapi.feature.project.viewmodel.ProjectViewModel
import ba.fluxor.fetchapi.feature.project_tree.viewmodel.ProjectTreeViewModel
import ba.fluxor.fetchapi.feature.request.viewmodel.RequestViewModel
import ba.fluxor.fetchapi.feature.sub_project.viewmodel.SubProjectViewModel
import ba.fluxor.fetchapi.feature.tabs.viewmodel.TabsViewModel
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.search
import fetchapi.composeapp.generated.resources.select_project
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProjectTreeViewPanel(
  treeVm: ProjectTreeViewModel = koinViewModel(),
  projectVm: ProjectViewModel = koinViewModel(),
  subProjectVm: SubProjectViewModel = koinViewModel(),
  folderVm: FolderViewModel = koinViewModel(),
  requestVm: RequestViewModel = koinViewModel(),
  tabsVm: TabsViewModel = koinViewModel(),
) {
  val treeState by treeVm.state.collectAsStateWithLifecycle()
  val projectState by projectVm.state.collectAsStateWithLifecycle()
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
      CompactInput(
        value = query,
        onValueChange = { query = it },
        placeholder = stringResource(Res.string.search),
        modifier = Modifier.weight(1f),
        enabled = projectState.active != null,
      )
      SquareIconButton(
        icon = Icons.Default.Add,
        onClick = {
          val projectId = projectState.active?.id ?: return@SquareIconButton
          subProjectVm.createSubProject(projectId)
        },
        enabled = projectState.active != null,
      )
    }

    Box(modifier = Modifier.fillMaxSize().padding(top = 12.dp)) {
      if (projectState.active == null) {
        Text(
          text = stringResource(Res.string.select_project),
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
          tabsVm = tabsVm,
        )
      }
    }
  }
}
