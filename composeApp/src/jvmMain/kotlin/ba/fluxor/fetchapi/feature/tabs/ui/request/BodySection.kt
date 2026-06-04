package ba.fluxor.fetchapi.feature.tabs.ui.request

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.component.CompactInput
import ba.fluxor.fetchapi.component.KeyValueDescTable
import ba.fluxor.fetchapi.component.SquareIconButton
import ba.fluxor.fetchapi.component.SquareOutlineButton
import ba.fluxor.fetchapi.feature.request.data.BodyConfig
import ba.fluxor.fetchapi.feature.request.data.FormDataEntry
import ba.fluxor.fetchapi.feature.tabs.ui.request.raw.RawBodyEditor
import ba.fluxor.fetchapi.feature.tabs.viewmodel.BodyDrafts
import ba.fluxor.fetchapi.feature.tabs.ui.request.raw.RawToolbar
import ba.fluxor.fetchapi.feature.tabs.ui.request.raw.format.formatter
import fetchapi.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

private enum class BodyKind {
  NONE,
  RAW,
  FORM_DATA,
  URL_ENCODED,
  BINARY
}

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
  drafts: BodyDrafts,
  onChange: (BodyConfig, BodyDrafts) -> Unit,
) {
  val onBodyChange: (BodyConfig) -> Unit = { onChange(it, drafts) }

  Column(modifier = Modifier.fillMaxWidth()) {

    val topRowScroll = rememberScrollState()

    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
        .horizontalScroll(topRowScroll),
    ) {
      BodyKind.entries.forEach { kind ->
        SquareOutlineButton(
          text = kind.label(),
          onClick = {
            val stashed = drafts.stash(body)
            onChange(restore(kind, stashed), stashed)
          },
          modifier = Modifier.padding(end = 8.dp)
            .height(32.dp)
            .padding(horizontal = 12.dp),
          borderWidth = if (body.kind() == kind) 2.dp else 0.dp,
        )
      }
      if (body is BodyConfig.Raw) {
        Spacer(Modifier.width(8.dp))
        RawToolbar(
          language = body.language,
          onLanguageChange = { newLang ->
            val formatted = newLang.formatter()
              ?.format(body.content)
              ?.getOrNull()
              ?: body.content
            onBodyChange(body.copy(language = newLang, content = formatted))
          },
          beautifyEnabled = body.language.formatter() != null && body.content.isNotBlank(),
          onBeautify = {
            body.language.formatter()
              ?.format(body.content)
              ?.onSuccess { formatted ->
                if (formatted != body.content) onBodyChange(body.copy(content = formatted))
              }
          },
        )
      }
    }

    HorizontalScrollbar(
      adapter = rememberScrollbarAdapter(topRowScroll),
      modifier = Modifier
        .fillMaxWidth()
        .height(2.dp)
        .padding(horizontal = 4.dp),
      style = LocalScrollbarStyle.current.copy(
        unhoverColor = MaterialTheme.colorScheme.outlineVariant,
        hoverColor = MaterialTheme.colorScheme.primary,
      ),
    )

    Spacer(Modifier.height(12.dp))

    when (body) {
      BodyConfig.None -> NoneEditor()
      is BodyConfig.Raw -> RawBodyEditor(body, onBodyChange)
      is BodyConfig.FormData -> FormDataEditor(body, onBodyChange)
      is BodyConfig.UrlEncoded -> KeyValueDescTable(
        rows = body.fields,
        onChange = { onBodyChange(BodyConfig.UrlEncoded(it)) },
        modifier = Modifier.fillMaxWidth(),
      )

      is BodyConfig.Binary -> BinaryEditor(body, onBodyChange)
    }
  }
}

private fun restore(kind: BodyKind, drafts: BodyDrafts): BodyConfig = when (kind) {
  BodyKind.NONE -> BodyConfig.None
  BodyKind.RAW -> drafts.raw
  BodyKind.FORM_DATA -> drafts.formData
  BodyKind.URL_ENCODED -> drafts.urlEncoded
  BodyKind.BINARY -> drafts.binary
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
private fun FormDataEditor(body: BodyConfig.FormData, onChange: (BodyConfig) -> Unit) {
  val rows = body.fields + FormDataEntry()

  Box(modifier = Modifier.fillMaxWidth()) {
    val scroll = rememberScrollState()
    Column(
      modifier = Modifier.fillMaxWidth()
        .verticalScroll(scroll)
    ) {
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
              onChange(BodyConfig.FormData(body.fields.toMutableList()
                .also { it.removeAt(index) }))
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
  return current.toMutableList()
    .also { it[index] = next }
}

@Composable
private fun FormDataHeader() {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth()
      .padding(vertical = 6.dp),
  ) {
    Spacer(Modifier.width(40.dp))
    Text(
      stringResource(Res.string.variables_key),
      modifier = Modifier.weight(1f)
        .padding(horizontal = 4.dp),
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.secondary
    )
    Text(
      stringResource(Res.string.api_key_value),
      modifier = Modifier.weight(1f)
        .padding(horizontal = 4.dp),
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.secondary
    )
    Text(
      stringResource(Res.string.description),
      modifier = Modifier.weight(1f)
        .padding(horizontal = 4.dp),
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.secondary
    )
    Box(
      modifier = Modifier.width(60.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        stringResource(Res.string.is_file),
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.secondary
      )
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
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
  ) {
    Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
      Checkbox(checked = entry.enabled, onCheckedChange = { onChange(entry.copy(enabled = it)) })
    }
    CompactInput(
      value = entry.key,
      onValueChange = { onChange(entry.copy(key = it)) },
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 4.dp),
      placeholder = stringResource(Res.string.variables_key),
    )
    CompactInput(
      value = entry.value,
      onValueChange = { onChange(entry.copy(value = it)) },
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 4.dp),
      placeholder = stringResource(Res.string.api_key_value),
    )
    CompactInput(
      value = entry.description,
      onValueChange = { onChange(entry.copy(description = it)) },
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 4.dp),
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
      modifier = Modifier
        .padding(horizontal = 12.dp)
        .height(32.dp)
        .padding(horizontal = 12.dp),
    )
    Spacer(Modifier.width(12.dp))
    Text(
      text = body.filePath ?: stringResource(Res.string.no_file_chosen),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}
