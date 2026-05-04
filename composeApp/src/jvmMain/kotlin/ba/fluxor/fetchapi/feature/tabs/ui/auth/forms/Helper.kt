package ba.fluxor.fetchapi.feature.tabs.ui.auth.forms

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.component.CompactInput
import ba.fluxor.fetchapi.component.SquareOutlineButton
import ba.fluxor.fetchapi.feature.sub_project.data.auth.Auth
import ba.fluxor.fetchapi.feature.sub_project.data.auth.AuthTypes
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.add_to
import fetchapi.composeapp.generated.resources.add_to_header
import fetchapi.composeapp.generated.resources.add_to_query
import fetchapi.composeapp.generated.resources.auth_type
import org.jetbrains.compose.resources.stringResource

@Composable
fun CheckboxRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
  ) {
    Checkbox(checked = checked, onCheckedChange = onChange)
    Spacer(Modifier.width(4.dp))
    Text(label)
  }
}

@Composable
fun AuthTypeDropdown(selected: String, onSelect: (String) -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  var buttonWidth by remember { mutableStateOf(0.dp) }
  val density = LocalDensity.current

  Text(
    text = stringResource(Res.string.auth_type),
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.secondary,
  )
  Spacer(modifier = Modifier.size(4.dp))
  Box(
    modifier = Modifier.padding(end = 16.dp)
  ) {
    SquareOutlineButton(
      text = selected,
      onClick = { expanded = true },
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth()
        .onGloballyPositioned { coordinates ->
          buttonWidth = with(density) { coordinates.size.width.toDp() + 30.dp }
        },
      contentAlignment = Alignment.CenterStart,
    )
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier.width(buttonWidth),
    ) {
      Box(modifier = Modifier.height(300.dp).fillMaxWidth()) {
        val scrollState = rememberScrollState()
        Column(
          modifier = Modifier.fillMaxWidth().verticalScroll(scrollState),
        ) {
          AuthTypes.ALL.forEach { type ->
            DropdownMenuItem(
              text = {
                Text(text = type, maxLines = 1, overflow = TextOverflow.Ellipsis)
              },
              onClick = {
                onSelect(type)
                expanded = false
              },
              leadingIcon = {
                if (selected == type) {
                  Icon(imageVector = Icons.Default.Check, contentDescription = null)
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
            hoverColor = MaterialTheme.colorScheme.primary,
          ),
        )
      }
    }
  }
}

@Composable
fun <T> EnumDropdown(
  label: String,
  options: List<T>,
  selected: T,
  onSelect: (T) -> Unit,
  optionLabel: @Composable (T) -> String,
) {
  var expanded by remember { mutableStateOf(false) }
  var buttonWidth by remember { mutableStateOf(0.dp) }
  val density = LocalDensity.current

  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(text = label)
    Box {
      SquareOutlineButton(
        text = optionLabel(selected),
        onClick = { expanded = true },
        modifier = Modifier
          .width(400.dp)
          .height(32.dp)
          .onGloballyPositioned { coordinates ->
            buttonWidth = with(density) { coordinates.size.width.toDp() + 30.dp }
          }
          .padding(start = 8.dp),
        contentAlignment = Alignment.CenterStart,
      )
      DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.width(buttonWidth),
      ) {
        options.forEach { option ->
          DropdownMenuItem(
            text = { Text(text = optionLabel(option), maxLines = 1, overflow = TextOverflow.Ellipsis) },
            onClick = {
              onSelect(option)
              expanded = false
            },
            leadingIcon = {
              if (option == selected) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
              }
            },
          )
        }
      }
    }
  }
}

@Composable
fun AddToDropdown(selected: Auth.AddTo, onSelect: (Auth.AddTo) -> Unit) {
  EnumDropdown(
    label = stringResource(Res.string.add_to),
    options = listOf(Auth.AddTo.HEADER, Auth.AddTo.QUERY),
    selected = selected,
    onSelect = onSelect,
    optionLabel = { addToLabel(it) },
  )
}

@Composable
private fun addToLabel(value: Auth.AddTo): String = when (value) {
  Auth.AddTo.HEADER -> stringResource(Res.string.add_to_header)
  Auth.AddTo.QUERY -> stringResource(Res.string.add_to_query)
}

@Composable
fun FieldText(
  label: String,
  value: String,
  singleLine: Boolean = true,
  minHeight: androidx.compose.ui.unit.Dp = 0.dp,
  onChange: (String) -> Unit,
) {
  Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
    Row (
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {

      Text(
        text = label,
      )
      CompactInput(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.width(400.dp),
        placeholder = label,
      )
    }
  }
}
