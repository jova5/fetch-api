package ba.fluxor.fetchapi.feature.tabs.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.component.CompactInput
import ba.fluxor.fetchapi.component.SquareButton
import ba.fluxor.fetchapi.component.SquareOutlineButton
import ba.fluxor.fetchapi.feature.tabs.viewmodel.SubProjectTab
import ba.fluxor.fetchapi.feature.tabs.viewmodel.TabBuffer
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.auth_config
import fetchapi.composeapp.generated.resources.auth_type
import fetchapi.composeapp.generated.resources.save
import org.jetbrains.compose.resources.stringResource

private val AUTH_TYPES = listOf(
  "NONE",
  "BASIC AUTH",
  "BEARER TOKEN",
  "JWT BEARER",
  "DIGEST AUTH",
  "OAUTH 1.0",
  "OAUTH 2.0",
  "API_KEY",
  "CUSTOM"
)

@Composable
fun SubProjectTabEditor(
  buffer: TabBuffer.SubProject,
  isDirty: Boolean,
  onChange: (TabBuffer) -> Unit,
  onSave: () -> Unit,
) {

  var selectedTab by remember { mutableStateOf(SubProjectTab.entries.first()) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      CompactInput(
        value = buffer.name,
        onValueChange = { onChange(buffer.copy(name = it)) }
      )
      Spacer(Modifier.weight(1f))
      SquareButton(
        text = stringResource(Res.string.save),
        onClick = onSave,
        enabled = isDirty,
        modifier = Modifier
          .padding(horizontal = 16.dp, vertical = 6.dp)
      )
    }
    Spacer(Modifier.height(20.dp))

    Column (
      modifier = Modifier.widthIn(max = 800.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        SubProjectTab.entries.forEach { subProject ->
          SquareOutlineButton(
            text = stringResource(subProject.labelRes()),
            onClick = {
              selectedTab = subProject
            },
            modifier = Modifier.padding(horizontal = 16.dp),
            borderWidth = if (selectedTab == subProject) 2.dp else 0.dp
          )
        }
      }

      Spacer(Modifier.height(24.dp))

      AuthTypeDropdown(
        selected = buffer.authType,
        onSelect = { onChange(buffer.copy(authType = it)) },
      )
      Spacer(Modifier.height(12.dp))
      OutlinedTextField(
        value = buffer.authConfig.orEmpty(),
        onValueChange = { onChange(buffer.copy(authConfig = it.ifBlank { null })) },
        label = { Text(stringResource(Res.string.auth_config)) },
        modifier = Modifier.fillMaxWidth()
          .height(160.dp),
      )
    }
  }
}

@Composable
private fun AuthTypeDropdown(selected: String, onSelect: (String) -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  var buttonWidth by remember { mutableStateOf(0.dp) }
  val density = LocalDensity.current

  Text(
    text = stringResource(Res.string.auth_type),
  )
  Box {
    SquareOutlineButton(
      text = selected,
      onClick = {
        expanded = true
      },
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth()
        .onGloballyPositioned { coordinates ->
          buttonWidth = with(density) { coordinates.size.width.toDp() + 30.dp }
        },
      contentAlignment = Alignment.CenterStart
    )
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier
        .width(buttonWidth)
    ) {
      Box(modifier = Modifier
        .height(300.dp)
        .fillMaxWidth()
      ) {
        val scrollState = rememberScrollState()
        Column (
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        ) {
          AUTH_TYPES.forEach { type ->
            DropdownMenuItem(
              text = {
                Text(
                  text = type,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                     },
              onClick = {
                onSelect(type)
                expanded = false
              },
              leadingIcon = {
                if (selected == type) {
                  Icon(imageVector = Icons.Default.Check, contentDescription = "Selected", )
                }
              }
            )
          }
        }
        VerticalScrollbar(
          modifier = Modifier
            .width(4.dp)
            .align(Alignment.CenterEnd)
            .fillMaxHeight(),
          adapter = rememberScrollbarAdapter(scrollState = scrollState),
          style = LocalScrollbarStyle.current.copy(
            unhoverColor = MaterialTheme.colorScheme.outlineVariant,
            hoverColor = MaterialTheme.colorScheme.primary
          ),
        )
      }
    }
  }
}
