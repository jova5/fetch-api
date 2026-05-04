package ba.fluxor.fetchapi.feature.tabs.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.component.CompactInput
import ba.fluxor.fetchapi.component.SquareButton
import ba.fluxor.fetchapi.component.SquareOutlineButton
import ba.fluxor.fetchapi.feature.tabs.ui.auth.AuthorizationSection
import ba.fluxor.fetchapi.feature.tabs.ui.variables.VariablesSection
import ba.fluxor.fetchapi.feature.tabs.viewmodel.SubProjectTab
import ba.fluxor.fetchapi.feature.tabs.viewmodel.TabBuffer
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.save
import org.jetbrains.compose.resources.stringResource

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
    Spacer(Modifier.height(8.dp))
    Divider(thickness = 1.dp)
    Spacer(Modifier.height(20.dp))

    Column(
      modifier = Modifier.widthIn(max = 800.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        SubProjectTab.entries.forEach { tab ->
          SquareOutlineButton(
            text = stringResource(tab.labelRes()),
            onClick = { selectedTab = tab },
            modifier = Modifier.padding(horizontal = 16.dp),
            borderWidth = if (selectedTab == tab) 2.dp else 0.dp
          )
        }
      }
      Spacer(Modifier.height(8.dp))
      Divider(thickness = 1.dp)

      Spacer(Modifier.height(24.dp))

      when (selectedTab) {
        SubProjectTab.AUTHORIZATION -> AuthorizationSection(
          authType = buffer.authType,
          authConfig = buffer.authConfig,
          onChange = { newType, newConfig ->
            onChange(buffer.copy(authType = newType, authConfig = newConfig))
          },
        )
        SubProjectTab.VARIABLES -> VariablesSection(
          variables = buffer.variables,
          onChange = { newVars -> onChange(buffer.copy(variables = newVars)) },
        )
      }
    }
  }
}
