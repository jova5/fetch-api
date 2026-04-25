package ba.fluxor.fetchapi.feature.project.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ba.fluxor.fetchapi.component.CompactInput
import ba.fluxor.fetchapi.component.SquareIconButton
import ba.fluxor.fetchapi.component.TooltipBelow
import ba.fluxor.fetchapi.feature.project.viewmodel.ProjectViewModel
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.manage_projects
import fetchapi.composeapp.generated.resources.search
import fetchapi.composeapp.generated.resources.select_project
import org.jetbrains.compose.resources.stringResource
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

  Box(
    modifier = Modifier
      .height(32.dp)
  ) {
    OutlinedButton(
      onClick = { expanded = true },
      modifier = Modifier.widthIn(max = 150.dp),
      shape = RoundedCornerShape(4.dp),
      contentPadding = PaddingValues(
        start = 12.dp,
        top = 0.dp,
        end = 8.dp,
        bottom = 0.dp
      ),
      border = BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
      )
    ) {
      TooltipBelow(
        text = projectState.active?.name ?: stringResource(Res.string.select_project)
      ) {
        Row (
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = projectState.active?.name ?: stringResource(Res.string.select_project),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
              .weight(1f),
          )
          Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
          )
        }
      }
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier
        .width(320.dp),
    ) {
      val focusManager = LocalFocusManager.current

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 0.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
          .pointerInput(Unit) {
            detectTapGestures { focusManager.clearFocus() }
          },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        CompactInput(
          value = query,
          onValueChange = { query = it },
          placeholder = stringResource(Res.string.search),
          modifier = Modifier.weight(1f),
        )
        SquareIconButton(
          onClick = {
            showManager = true
            expanded = false
          },
          icon = Icons.Default.Edit,
        )
      }

      val filtered = remember(projectState.projects, query) {
        if (query.isBlank()) projectState.projects
        else projectState.projects.filter { it.name.contains(query, ignoreCase = true) }
      }

      if (filtered.isEmpty()) {
        DropdownMenuItem(
          text = {
            Text(stringResource(Res.string.manage_projects),
              color = MaterialTheme.colorScheme.onSurfaceVariant)
          },
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
