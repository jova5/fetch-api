package ba.fluxor.fetchapi.feature.tabs.ui.request.raw

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ba.fluxor.fetchapi.component.SimpleDropdown
import ba.fluxor.fetchapi.component.SquareOutlineButton
import ba.fluxor.fetchapi.component.highlightJson
import ba.fluxor.fetchapi.feature.request.data.BodyConfig
import ba.fluxor.fetchapi.feature.request.data.RawLanguage
import ba.fluxor.fetchapi.feature.tabs.ui.request.raw.format.RawFormatter
import ba.fluxor.fetchapi.feature.tabs.ui.request.raw.format.formatter
import com.wakaztahir.codeeditor.model.CodeLang
import com.wakaztahir.codeeditor.prettify.PrettifyParser
import com.wakaztahir.codeeditor.theme.CodeTheme
import com.wakaztahir.codeeditor.theme.DefaultTheme
import com.wakaztahir.codeeditor.theme.MonokaiTheme
import com.wakaztahir.codeeditor.utils.parseCodeAsAnnotatedString
import fetchapi.composeapp.generated.resources.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource

private const val PASTE_THRESHOLD = 50
private const val HIGHLIGHT_MAX_CHARS = 500_000
private const val VALIDATION_DEBOUNCE_MS = 250L

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RawBodyEditor(
  body: BodyConfig.Raw,
  onChange: (BodyConfig) -> Unit,
  modifier: Modifier = Modifier,
) {
  val parser = remember { PrettifyParser() }
  val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
  val theme: CodeTheme = remember(isDark) { if (isDark) MonokaiTheme() else DefaultTheme() }
  val codeLang = body.language.toCodeLang()
  val highlightEnabled = body.content.length <= HIGHLIGHT_MAX_CHARS

  val formatter = body.language.formatter()
  var errorMessage by remember { mutableStateOf<String?>(null) }
  val invalidLabel = formatter?.let {
    stringResource(Res.string.body_raw_invalid_content, body.language.localizedShortLabel())
  }

  LaunchedEffect(body.content, body.language) {

    if (formatter == null || body.content.isBlank()) {
      errorMessage = null
      return@LaunchedEffect
    }

    delay(VALIDATION_DEBOUNCE_MS)

    errorMessage = withContext(Dispatchers.Default) {
      formatter.validate(body.content)
        .fold(
          onSuccess = { null },
          onFailure = { ex -> ex.message ?: invalidLabel ?: "Invalid" }
        )
    }
  }

  Column(modifier = modifier.fillMaxSize()) {
    RawToolbar(
      language = body.language,
      onLanguageChange = { newLang ->
        val formatted = newLang.formatter()
          ?.format(body.content)
          ?.getOrNull()
          ?: body.content
        onChange(body.copy(language = newLang, content = formatted))
      },
      beautifyEnabled = formatter != null && body.content.isNotBlank(),
      onBeautify = {
        formatter?.format(body.content)
          ?.onSuccess { formatted ->
            if (formatted != body.content) onChange(body.copy(content = formatted))
          }
      },
    )
    Spacer(Modifier.height(8.dp))
    HighlightedEditor(
      content = body.content,
      onContentChange = { next -> onChange(body.copy(content = next)) },
      parser = parser,
      theme = theme,
      codeLang = codeLang,
      isDark = isDark,
      highlightEnabled = highlightEnabled,
      formatter = formatter,
      errorMessage = errorMessage,
      modifier = Modifier.fillMaxSize(),
    )
  }
}

@Composable
private fun RawToolbar(
  language: RawLanguage,
  onLanguageChange: (RawLanguage) -> Unit,
  beautifyEnabled: Boolean,
  onBeautify: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    SimpleDropdown(
      options = RawLanguage.entries,
      selected = language,
      onSelect = onLanguageChange,
      width = 200.dp,
      optionLabel = { it.localizedShortLabel() },
    )
    SquareOutlineButton(
      text = stringResource(Res.string.body_raw_beautify),
      onClick = onBeautify,
      enabled = beautifyEnabled,
      borderWidth = 1.dp,
      modifier = Modifier
        .height(32.dp)
        .width(110.dp),
    )
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HighlightedEditor(
  content: String,
  onContentChange: (String) -> Unit,
  parser: PrettifyParser,
  theme: CodeTheme,
  codeLang: CodeLang,
  isDark: Boolean,
  highlightEnabled: Boolean,
  formatter: RawFormatter?,
  errorMessage: String?,
  modifier: Modifier = Modifier,
) {
  var textFieldValue by remember {
    mutableStateOf(
      TextFieldValue(
        annotatedString = renderHighlight(content, parser, theme, codeLang, isDark,
          highlightEnabled),
        selection = TextRange(content.length),
      )
    )
  }

  LaunchedEffect(content, codeLang, theme, isDark) {

    if (textFieldValue.text != content) {
      textFieldValue = TextFieldValue(
        annotatedString = renderHighlight(content, parser, theme, codeLang, isDark,
          highlightEnabled),
        selection = TextRange(content.length),
      )
    } else {
      textFieldValue = textFieldValue.copy(
        annotatedString = renderHighlight(content, parser, theme, codeLang, isDark,
          highlightEnabled),
      )
    }
  }

  val borderColor = if (errorMessage != null) {
    MaterialTheme.colorScheme.error
  } else {
    MaterialTheme.colorScheme.outlineVariant
  }
  val borderWidth = if (errorMessage != null) 2.dp else 1.dp
  val shape = RoundedCornerShape(4.dp)
  val plainColor = MaterialTheme.colorScheme.onSurface

  Box(modifier = modifier) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .clip(shape)
        .background(MaterialTheme.colorScheme.surface)
        .border(borderWidth, borderColor, shape)
        .padding(8.dp),
    ) {
      BasicTextField(
        value = textFieldValue,
        onValueChange = { newValue ->

          val previousText = textFieldValue.text
          val delta = newValue.text.length - previousText.length
          val looksLikePaste =
            delta > PASTE_THRESHOLD && newValue.text.contains('\n') && formatter != null

          val (finalText, formattedFromPaste) = if (looksLikePaste) {
            val formatted = formatter.format(newValue.text)
              .getOrNull()
            if (formatted != null && formatted != newValue.text) formatted to true
            else newValue.text to false
          } else {
            newValue.text to false
          }

          val newSelection = if (formattedFromPaste) {
            TextRange(finalText.length)
          } else {
            newValue.selection
          }

          textFieldValue = TextFieldValue(
            annotatedString = renderHighlight(finalText, parser, theme, codeLang, isDark,
              highlightEnabled),
            selection = newSelection,
          )

          if (finalText != previousText) onContentChange(finalText)
        },
        modifier = Modifier.fillMaxSize(),
        textStyle = TextStyle(
          fontFamily = FontFamily.Monospace,
          fontSize = 14.sp,
          color = plainColor,
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
      )
    }

    if (errorMessage != null) {
      ErrorBadge(
        message = errorMessage,
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(8.dp),
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ErrorBadge(message: String, modifier: Modifier = Modifier) {
  TooltipArea(
    tooltip = {
      Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = RoundedCornerShape(4.dp),
        shadowElevation = 4.dp,
      ) {
        Text(
          text = message,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          style = MaterialTheme.typography.bodySmall,
        )
      }
    },
    delayMillis = 200,
    tooltipPlacement = TooltipPlacement.CursorPoint(
      offset = DpOffset(0.dp, 16.dp),
    ),
    modifier = modifier,
  ) {
    Box(
      modifier = Modifier
        .size(10.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.error),
    )
  }
}

private fun renderHighlight(
  code: String,
  parser: PrettifyParser,
  theme: CodeTheme,
  lang: CodeLang,
  isDark: Boolean,
  highlightEnabled: Boolean,
): AnnotatedString = when {
  !highlightEnabled || code.isEmpty() -> AnnotatedString(code)
  lang == CodeLang.JSON -> highlightJson(code, isDark)
  else -> runCatching { parseCodeAsAnnotatedString(parser, theme, lang, code) }
    .getOrDefault(AnnotatedString(code))
}

private fun RawLanguage.toCodeLang(): CodeLang = when (this) {
  RawLanguage.JSON -> CodeLang.JSON
  RawLanguage.XML -> CodeLang.XML
  RawLanguage.HTML -> CodeLang.HTML
  RawLanguage.JAVASCRIPT -> CodeLang.JavaScript
  RawLanguage.TEXT -> CodeLang.Default
}

@Composable
private fun RawLanguage.localizedShortLabel(): String = when (this) {
  RawLanguage.JSON -> stringResource(Res.string.lang_json)
  RawLanguage.XML -> stringResource(Res.string.lang_xml)
  RawLanguage.TEXT -> stringResource(Res.string.lang_text)
  RawLanguage.HTML -> stringResource(Res.string.lang_html)
  RawLanguage.JAVASCRIPT -> stringResource(Res.string.lang_javascript)
}
