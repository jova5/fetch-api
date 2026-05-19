package ba.fluxor.fetchapi.feature.tabs.ui.request

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ba.fluxor.fetchapi.component.CompactInput
import ba.fluxor.fetchapi.component.KeyValueDescTable
import ba.fluxor.fetchapi.component.SimpleDropdown
import ba.fluxor.fetchapi.component.SquareIconButton
import ba.fluxor.fetchapi.component.SquareOutlineButton
import ba.fluxor.fetchapi.feature.request.data.BodyConfig
import ba.fluxor.fetchapi.feature.request.data.FormDataEntry
import ba.fluxor.fetchapi.feature.request.data.RawLanguage
import fetchapi.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

private enum class BodyKind { NONE, RAW, FORM_DATA, URL_ENCODED, BINARY }

private fun BodyConfig.kind(): BodyKind = when (this) {
  BodyConfig.None -> BodyKind.NONE
  is BodyConfig.Raw -> BodyKind.RAW
  is BodyConfig.FormData -> BodyKind.FORM_DATA
  is BodyConfig.UrlEncoded -> BodyKind.URL_ENCODED
  is BodyConfig.Binary -> BodyKind.BINARY
}

@Composable
private fun BodyKind.label(): String = when (this) {
  BodyKind.NONE -> stringResource(Res.string.body_none)
  BodyKind.RAW -> stringResource(Res.string.body_raw)
  BodyKind.FORM_DATA -> stringResource(Res.string.body_form_data)
  BodyKind.URL_ENCODED -> stringResource(Res.string.body_url_encoded)
  BodyKind.BINARY -> stringResource(Res.string.body_binary)
}

@Composable
fun BodySection(
  body: BodyConfig,
  onChange: (BodyConfig) -> Unit,
) {
  Column(modifier = Modifier.fillMaxWidth()) {

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
      BodyKind.entries.forEach { kind ->
        SquareOutlineButton(
          text = kind.label(),
          onClick = { onChange(switchTo(kind, body)) },
          modifier = Modifier.padding(end = 8.dp).height(32.dp).padding(horizontal = 12.dp),
          borderWidth = if (body.kind() == kind) 2.dp else 0.dp,
        )
      }
    }
    Spacer(Modifier.height(12.dp))

    when (body) {
      BodyConfig.None -> NoneEditor()
      is BodyConfig.Raw -> RawEditor(body, onChange)
      is BodyConfig.FormData -> FormDataEditor(body, onChange)
      is BodyConfig.UrlEncoded -> KeyValueDescTable(
        rows = body.fields,
        onChange = { onChange(BodyConfig.UrlEncoded(it)) },
        modifier = Modifier.fillMaxWidth(),
        showHideButton = false
      )
      is BodyConfig.Binary -> BinaryEditor(body, onChange)
    }
  }
}

private fun switchTo(target: BodyKind, current: BodyConfig): BodyConfig = when (target) {
  BodyKind.NONE -> BodyConfig.None
  BodyKind.RAW -> if (current is BodyConfig.Raw) current else BodyConfig.Raw()
  BodyKind.FORM_DATA -> if (current is BodyConfig.FormData) current else BodyConfig.FormData()
  BodyKind.URL_ENCODED -> if (current is BodyConfig.UrlEncoded) current else BodyConfig.UrlEncoded()
  BodyKind.BINARY -> if (current is BodyConfig.Binary) current else BodyConfig.Binary()
}

@Composable
private fun NoneEditor() {
  Text(
    text = stringResource(Res.string.body_none_description),
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
  )
}

@Composable
private fun RawEditor(body: BodyConfig.Raw, onChange: (BodyConfig) -> Unit) {
  Column(modifier = Modifier.fillMaxWidth()) {
    SimpleDropdown(
      options = RawLanguage.entries,
      selected = body.language,
      onSelect = { onChange(body.copy(language = it)) },
      width = 200.dp,
      optionLabel = { it.localizedLabel() },
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
      value = body.content,
      onValueChange = { onChange(body.copy(content = it)) },
      modifier = Modifier.fillMaxWidth().heightIn(min = 240.dp, max = 600.dp),
      textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
    )
  }
}

@Composable
private fun RawLanguage.localizedLabel(): String = when (this) {
  RawLanguage.JSON -> stringResource(Res.string.lang_json)
  RawLanguage.XML -> stringResource(Res.string.lang_xml)
  RawLanguage.TEXT -> stringResource(Res.string.lang_text)
  RawLanguage.HTML -> stringResource(Res.string.lang_html)
  RawLanguage.JAVASCRIPT -> stringResource(Res.string.lang_javascript)
}

@Composable
private fun FormDataEditor(body: BodyConfig.FormData, onChange: (BodyConfig) -> Unit) {
  val rows = body.fields + FormDataEntry()

  Box(modifier = Modifier.fillMaxWidth()) {
    val scroll = rememberScrollState()
    Column(modifier = Modifier.fillMaxWidth().verticalScroll(scroll)) {
      FormDataHeader()
      rows.forEachIndexed { index, entry ->
        val isTrailing = index == rows.lastIndex
        FormDataRow(
          entry = entry,
          showDelete = !isTrailing,
          onChange = { next ->
            onChange(BodyConfig.FormData(formDataUpdateAt(body.fields, index, isTrailing, next)))
          },
          onDelete = {
            if (!isTrailing) {
              onChange(BodyConfig.FormData(body.fields.toMutableList().also { it.removeAt(index) }))
            }
          },
        )
      }
    }
    VerticalScrollbar(
      modifier = Modifier.width(4.dp).align(Alignment.CenterEnd).fillMaxHeight(),
      adapter = rememberScrollbarAdapter(scroll),
      style = LocalScrollbarStyle.current.copy(
        unhoverColor = MaterialTheme.colorScheme.outlineVariant,
        hoverColor = MaterialTheme.colorScheme.primary,
      ),
    )
  }
}

private fun formDataUpdateAt(
  current: List<FormDataEntry>,
  index: Int,
  isTrailing: Boolean,
  next: FormDataEntry,
): List<FormDataEntry> {
  if (isTrailing) {
    if (next.key.isBlank() && next.value.isBlank() && next.description.isBlank()) return current
    return current + next
  }
  return current.toMutableList().also { it[index] = next }
}

@Composable
private fun FormDataHeader() {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
  ) {
    Spacer(Modifier.width(40.dp))
    Text(stringResource(Res.string.variables_key), modifier = Modifier.weight(1f).padding(horizontal = 4.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
    Text(stringResource(Res.string.api_key_value), modifier = Modifier.weight(1f).padding(horizontal = 4.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
    Text(stringResource(Res.string.description), modifier = Modifier.weight(1f).padding(horizontal = 4.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
    Box(modifier = Modifier.width(60.dp), contentAlignment = Alignment.Center) {
      Text(stringResource(Res.string.is_file), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
    }
    Spacer(Modifier.width(40.dp))
  }
}

@Composable
private fun FormDataRow(
  entry: FormDataEntry,
  showDelete: Boolean,
  onChange: (FormDataEntry) -> Unit,
  onDelete: () -> Unit,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
  ) {
    Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
      Checkbox(checked = entry.enabled, onCheckedChange = { onChange(entry.copy(enabled = it)) })
    }
    CompactInput(
      value = entry.key,
      onValueChange = { onChange(entry.copy(key = it)) },
      modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
      placeholder = stringResource(Res.string.variables_key),
    )
    CompactInput(
      value = entry.value,
      onValueChange = { onChange(entry.copy(value = it)) },
      modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
      placeholder = stringResource(Res.string.api_key_value),
    )
    CompactInput(
      value = entry.description,
      onValueChange = { onChange(entry.copy(description = it)) },
      modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
      placeholder = stringResource(Res.string.description),
    )
    Box(modifier = Modifier.width(60.dp), contentAlignment = Alignment.Center) {
      Checkbox(checked = entry.isFile, onCheckedChange = { onChange(entry.copy(isFile = it)) })
    }
    Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
      if (showDelete) {
        SquareIconButton(icon = Icons.Default.Close, onClick = onDelete, borderWidth = 0.dp)
      }
    }
  }
}

@Composable
private fun BinaryEditor(body: BodyConfig.Binary, onChange: (BodyConfig) -> Unit) {
  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
    SquareOutlineButton(
      text = stringResource(Res.string.choose_file),
      onClick = { println("[FetchAPI] Binary file picker not yet implemented") },
      modifier = Modifier.padding(horizontal = 12.dp).height(32.dp).padding(horizontal = 12.dp),
    )
    Spacer(Modifier.width(12.dp))
    Text(
      text = body.filePath ?: stringResource(Res.string.no_file_chosen),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}
