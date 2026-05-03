package ba.fluxor.fetchapi.feature.tabs.ui.variables

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.component.CompactInput
import ba.fluxor.fetchapi.component.SquareIconButton
import ba.fluxor.fetchapi.feature.tabs.viewmodel.TabBuffer
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.variables_key
import fetchapi.composeapp.generated.resources.variables_value
import org.jetbrains.compose.resources.stringResource

@Composable
fun VariablesSection(
  variables: List<TabBuffer.VariableEntry>,
  onChange: (List<TabBuffer.VariableEntry>) -> Unit,
) {
  val rows = variables + TabBuffer.VariableEntry("", "")

  Box {

    val scrollState = rememberScrollState()

    Column(modifier = Modifier
      .fillMaxWidth()
      .verticalScroll(state = scrollState)
    ) {

      HeaderRow()

      rows.forEachIndexed { index, entry ->
        val isTrailing = index == rows.lastIndex
        VariableRow(
          entry = entry,
          showDelete = !isTrailing,
          onKeyChange = { newKey ->
            onChange(updateAt(variables, index, isTrailing, key = newKey, value = entry.value))
          },
          onValueChange = { newValue ->
            onChange(updateAt(variables, index, isTrailing, key = entry.key, value = newValue))
          },
          onDelete = {
            if (!isTrailing) {
              onChange(variables.toMutableList()
                .also { it.removeAt(index) })
            }
          },
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

private fun updateAt(
  current: List<TabBuffer.VariableEntry>,
  index: Int,
  isTrailing: Boolean,
  key: String,
  value: String,
): List<TabBuffer.VariableEntry> {
  val next = TabBuffer.VariableEntry(key, value)
  if (isTrailing) {
    if (key.isBlank() && value.isBlank()) return current
    return current + next
  }
  return current.toMutableList()
    .also { it[index] = next }
}

@Composable
private fun HeaderRow() {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth()
      .padding(vertical = 6.dp),
  ) {
    Text(
      text = stringResource(Res.string.variables_key),
      style = MaterialTheme.typography.labelLarge,
      modifier = Modifier.weight(1f)
        .padding(horizontal = 4.dp),
    )
    Text(
      text = stringResource(Res.string.variables_value),
      style = MaterialTheme.typography.labelLarge,
      modifier = Modifier.weight(1f)
        .padding(horizontal = 4.dp),
    )
    Spacer(Modifier.width(40.dp))
  }
}

@Composable
private fun VariableRow(
  entry: TabBuffer.VariableEntry,
  showDelete: Boolean,
  onKeyChange: (String) -> Unit,
  onValueChange: (String) -> Unit,
  onDelete: () -> Unit,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth()
      .padding(vertical = 4.dp),
  ) {
    CompactInput(
      value = entry.key,
      onValueChange = onKeyChange,
      modifier = Modifier.weight(1f)
        .padding(horizontal = 4.dp),
      placeholder = stringResource(Res.string.variables_key),
    )
    CompactInput(
      value = entry.value,
      onValueChange = onValueChange,
      modifier = Modifier.weight(1f)
        .padding(horizontal = 4.dp),
      placeholder = stringResource(Res.string.variables_value),
    )
    Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
      if (showDelete) {
        SquareIconButton(
          icon = Icons.Default.Close,
          onClick = onDelete,
          borderWidth = 0.dp,
        )
      }
    }
  }
}
