package ba.fluxor.fetchapi.component

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.feature.request.data.KeyValueEntry
import fetchapi.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun KeyValueDescTable(
  rows: List<KeyValueEntry>,
  onChange: (List<KeyValueEntry>) -> Unit,
  modifier: Modifier = Modifier,
  overriddenKeys: Set<String> = emptySet(),
  onReadOnlyToggle: (String, Boolean) -> Unit = { _, _ -> },
  autoHidden: Boolean = false,
  onToggleAutoHidden: () -> Unit = {},
  keyPlaceholder: String? = null,
) {
  val readOnlyEntries = rows.filter { it.readOnly }
  val editableEntries = rows.filter { !it.readOnly }
  val displayEditable = editableEntries + KeyValueEntry()
  val hasReadOnly = readOnlyEntries.isNotEmpty()

  Box(modifier = modifier) {
    val scrollState = rememberScrollState()

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(scrollState),
    ) {
      HeaderRow(
        keyPlaceholder = keyPlaceholder ?: stringResource(Res.string.variables_key),
        showHideButton = hasReadOnly,
        autoHidden = autoHidden,
        onToggleAutoHidden = onToggleAutoHidden,
      )

      if (!autoHidden) {
        readOnlyEntries.forEach { entry ->
          ReadOnlyRow(
            entry = entry,
            isOverridden = entry.key.lowercase() in overriddenKeys,
            onIncludedChange = { included -> onReadOnlyToggle(entry.key, included) },
          )
        }
      }

      displayEditable.forEachIndexed { index, entry ->
        val isTrailing = index == displayEditable.lastIndex
        EditableRow(
          entry = entry,
          showDelete = !isTrailing,
          onEnabledChange = {
            onChange(updateAt(editableEntries, index, isTrailing, entry.copy(enabled = it)))
          },
          onKeyChange = { onChange(updateAt(editableEntries, index, isTrailing, entry.copy(key = it))) },
          onValueChange = { onChange(updateAt(editableEntries, index, isTrailing, entry.copy(value = it))) },
          onDescriptionChange = {
            onChange(updateAt(editableEntries, index, isTrailing, entry.copy(description = it)))
          },
          onDelete = {
            if (!isTrailing) {
              onChange(editableEntries.toMutableList()
                .also { it.removeAt(index) })
            }
          },
          keyPlaceholder = keyPlaceholder,
        )
      }
    }
    VerticalScrollbar(
      modifier = Modifier.width(4.dp)
        .align(Alignment.CenterEnd)
        .fillMaxHeight(),
      adapter = rememberScrollbarAdapter(scrollState = scrollState),
      style = LocalScrollbarStyle.current.copy(
        unhoverColor = MaterialTheme.colorScheme.outlineVariant,
        hoverColor = MaterialTheme.colorScheme.primary,
      ),
    )
  }
}

private fun updateAt(
  current: List<KeyValueEntry>,
  index: Int,
  isTrailing: Boolean,
  next: KeyValueEntry,
): List<KeyValueEntry> {
  if (isTrailing) {
    if (next.key.isBlank() && next.value.isBlank() && next.description.isBlank()) return current
    return current + next
  }
  return current.toMutableList()
    .also { it[index] = next }
}

@Composable
private fun HeaderRow(
  keyPlaceholder: String,
  showHideButton: Boolean,
  autoHidden: Boolean,
  onToggleAutoHidden: () -> Unit,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
  ) {
    Spacer(Modifier.width(4.dp))

    if (showHideButton) {
      TooltipBelow(
        text = if (autoHidden) stringResource(Res.string.show_auto_generated)
        else stringResource(Res.string.hide_auto_generated),
      ) {
        SquareIconButton(
          icon = if (autoHidden) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
          onClick = onToggleAutoHidden,
          borderWidth = 0.dp,
        )
      }
      Spacer(Modifier.width(8.dp))
    } else {
      Spacer(Modifier.width(38.dp))
    }

    Text(
      text = keyPlaceholder,
      style = MaterialTheme.typography.bodyLarge,
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 4.dp),
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.secondary,
    )
    Text(
      text = stringResource(Res.string.api_key_value),
      style = MaterialTheme.typography.bodyLarge,
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 4.dp),
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.secondary,
    )
    Text(
      text = stringResource(Res.string.description),
      style = MaterialTheme.typography.bodyLarge,
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 4.dp),
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.secondary,
    )
    Spacer(Modifier.width(40.dp))
  }
}

@Composable
private fun EditableRow(
  entry: KeyValueEntry,
  showDelete: Boolean,
  onEnabledChange: (Boolean) -> Unit,
  onKeyChange: (String) -> Unit,
  onValueChange: (String) -> Unit,
  onDescriptionChange: (String) -> Unit,
  onDelete: () -> Unit,
  keyPlaceholder: String?,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
  ) {
    Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
      Checkbox(checked = entry.enabled, onCheckedChange = onEnabledChange)
    }
    CompactInput(
      value = entry.key,
      onValueChange = onKeyChange,
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 4.dp),
      placeholder = keyPlaceholder ?: stringResource(Res.string.variables_key),
    )
    CompactInput(
      value = entry.value,
      onValueChange = onValueChange,
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 4.dp),
      placeholder = stringResource(Res.string.api_key_value),
    )
    CompactInput(
      value = entry.description,
      onValueChange = onDescriptionChange,
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 4.dp),
      placeholder = stringResource(Res.string.description),
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

@Composable
private fun ReadOnlyRow(
  entry: KeyValueEntry,
  isOverridden: Boolean,
  onIncludedChange: (Boolean) -> Unit,
) {
  val baseAlpha = if (entry.enabled) 0.6f else 0.3f
  val textAlpha = if (isOverridden) baseAlpha * 0.6f else baseAlpha
  val decoration = if (isOverridden) TextDecoration.LineThrough else TextDecoration.None
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
  ) {
    Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
      Checkbox(checked = entry.enabled, onCheckedChange = onIncludedChange)
    }
    Text(
      text = entry.key,
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 8.dp)
        .alpha(textAlpha),
      color = MaterialTheme.colorScheme.onSurface,
      textDecoration = decoration,
    )
    Text(
      text = entry.value,
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 8.dp)
        .alpha(textAlpha),
      color = MaterialTheme.colorScheme.onSurface,
      textDecoration = decoration,
    )
    Text(
      text = entry.description,
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 8.dp)
        .alpha(textAlpha),
      color = MaterialTheme.colorScheme.onSurface,
      textDecoration = decoration,
    )
    Spacer(Modifier.width(40.dp))
  }
}
