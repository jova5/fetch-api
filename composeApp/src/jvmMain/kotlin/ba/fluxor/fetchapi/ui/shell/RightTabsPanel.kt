package ba.fluxor.fetchapi.ui.shell

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ba.fluxor.fetchapi.component.UnsavedChangesDialog
import ba.fluxor.fetchapi.feature.project.viewmodel.ProjectViewModel
import ba.fluxor.fetchapi.feature.tabs.ui.TabBar
import ba.fluxor.fetchapi.feature.tabs.ui.TabEditor
import ba.fluxor.fetchapi.feature.tabs.viewmodel.TabsViewModel
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.no_open_tabs
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RightTabsPanel(
  tabsVm: TabsViewModel = koinViewModel(),
  projectVm: ProjectViewModel = koinViewModel(),
  ) {
  val state by tabsVm.state.collectAsStateWithLifecycle()
  val projectState by projectVm.state.collectAsStateWithLifecycle()

  LaunchedEffect(projectState.active) {
    val projectId = projectState.active?.id
    if (projectId != null) {
      tabsVm.loadTabsForProject(projectId)
    } else {
      tabsVm.clear()
    }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    if (state.tabs.isEmpty()) {
      Box(
        modifier = Modifier.fillMaxWidth().height(40.dp).padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart,
      ) {
        Text(
          text = stringResource(Res.string.no_open_tabs),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      HorizontalDivider()
      Spacer(Modifier.weight(1f))
    } else {
      TabBar(
        tabs = state.tabs,
        selectedTabId = state.selectedTabId,
        onSelect = tabsVm::selectTab,
        onClose = tabsVm::closeTab,
        onMove = tabsVm::moveTab,
        onReorderEnd = tabsVm::persistTabOrder,
      )
      HorizontalDivider()
      val selected = state.selectedTab
      if (selected != null) {
        TabEditor(
          tab = selected,
          onChange = { tabsVm.updateBuffer(selected.id, it) },
          onSave = { tabsVm.saveTab(selected.id) },
          onSend = { tabsVm.sendRequest(selected.id) },
        )
      }
    }
  }

  val pending = state.tabs.find { it.id == state.pendingCloseTabId }
  if (pending != null) {
    UnsavedChangesDialog(
      entityName = pending.title,
      onSave = { tabsVm.saveAndClose(pending.id) },
      onDiscard = { tabsVm.discardAndClose(pending.id) },
      onCancel = tabsVm::cancelClose,
    )
  }
}
