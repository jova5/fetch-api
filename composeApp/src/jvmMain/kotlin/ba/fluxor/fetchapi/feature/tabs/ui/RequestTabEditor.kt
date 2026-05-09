package ba.fluxor.fetchapi.feature.tabs.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ba.fluxor.fetchapi.component.CompactInput
import ba.fluxor.fetchapi.component.SimpleDropdown
import ba.fluxor.fetchapi.component.SquareButton
import ba.fluxor.fetchapi.feature.tabs.viewmodel.TabBuffer
import ba.fluxor.fetchapi.network.http.HttpMethod
import fetchapi.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun RequestTabEditor(
  buffer: TabBuffer.Request,
  isDirty: Boolean,
  onChange: (TabBuffer) -> Unit,
  onSave: () -> Unit,
) {
  Column(modifier = Modifier.fillMaxSize()
    .padding(16.dp)) {

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
          .background(MaterialTheme.colorScheme.tertiary)
          .padding(horizontal = 16.dp, vertical = 6.dp)
      )
    }
    Spacer(Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
      SimpleDropdown(
        options = HttpMethod.entries.map { it.name },
        selected = buffer.method,
        onSelect = { onChange(buffer.copy(method = it)) },
        width = 100.dp,
        optionLabel = { it }
      )
      Spacer(Modifier.width(8.dp))
      CompactInput(
        value = buffer.url,
        onValueChange = { onChange(buffer.copy(url = it)) },
        placeholder = stringResource(Res.string.url),
        modifier = Modifier.weight(1f),
      )
      Spacer(Modifier.width(8.dp))
      SquareButton(
        text = stringResource(Res.string.send),
        onClick = {},
        modifier = Modifier
          .padding(horizontal = 16.dp, vertical = 6.dp)
      )
    }
    OutlinedTextField(
      value = buffer.headers.orEmpty(),
      onValueChange = { onChange(buffer.copy(headers = it.ifBlank { null })) },
      label = { Text(stringResource(Res.string.headers)) },
      modifier = Modifier.fillMaxWidth()
        .height(120.dp),
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
      value = buffer.body.orEmpty(),
      onValueChange = { onChange(buffer.copy(body = it.ifBlank { null })) },
      label = { Text(stringResource(Res.string.body)) },
      modifier = Modifier.fillMaxWidth()
        .weight(1f),
    )

    Spacer(Modifier.height(8.dp))

    val scrollState = rememberScrollState()
    val shape = RoundedCornerShape(4.dp)

    SelectionContainer(
      modifier = Modifier
        .border(1.dp, MaterialTheme.colorScheme.primary, shape = shape),
    ) {
      BasicTextField(
        value = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a",
        onValueChange = {},
        readOnly = true,
        modifier = Modifier
          .heightIn(min = 300.dp, max = 600.dp)
          .fillMaxWidth()
          .padding(12.dp)
          .verticalScroll(scrollState)
          .horizontalScroll(rememberScrollState()),
        textStyle = TextStyle(
          fontFamily = FontFamily.Monospace,
          fontSize = 14.sp,
        )
      )
    }
  }
}
