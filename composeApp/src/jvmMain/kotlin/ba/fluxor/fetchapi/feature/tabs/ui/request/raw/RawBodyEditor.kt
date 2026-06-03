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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
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
import kotlin.time.Duration.Companion.milliseconds

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

    delay(VALIDATION_DEBOUNCE_MS.milliseconds)

    errorMessage = withContext(Dispatchers.Default) {
      formatter.validate(body.content)
        .fold(
          onSuccess = { null },
          onFailure = { ex -> ex.message ?: invalidLabel ?: "Invalid" }
        )
    }
  }

  Column(modifier = modifier.fillMaxSize()) {
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
    MaterialTheme.colorScheme.primary
  }
  val borderWidth = if (errorMessage != null) 2.dp else 1.dp
  val shape = RoundedCornerShape(4.dp)
  val plainColor = MaterialTheme.colorScheme.onSurface

  val scrollState = rememberScrollState()
  val density = LocalDensity.current
  val textMeasurer = rememberTextMeasurer()

  // Shared text metrics: the gutter MUST use the same style as the field so line
  // heights line up exactly.
  val vPad = 8.dp
  val editorStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = 14.sp,
    color = plainColor,
  )
  val gutterStyle = editorStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
  val bandColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
  val dividerColor = MaterialTheme.colorScheme.outlineVariant

  // Layout result from the text field, shared by the gutter and the current-line band.
  var textLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
  var isFocused by remember { mutableStateOf(false) }

  // Start offset of every logical line; index k -> line number k+1.
  val lineStarts = remember(content) { lineStartOffsets(content) }
  val contentLength = content.length
  val cursorOffset = textFieldValue.selection.start

  // Uniform single-line metrics, used for gutter width and as a fallback before the
  // first layout pass.
  val sampleLine = remember(textMeasurer, editorStyle) {
    textMeasurer.measure(AnnotatedString("0"), editorStyle)
  }
  val lineHeightPx = sampleLine.size.height.toFloat()
  val digitCount = max(2, lineStarts.size.toString().length)
  val gutterWidth = with(density) { (digitCount * sampleLine.size.width).toDp() } + 16.dp

  Box(modifier = modifier) {
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .clip(shape)
        .background(MaterialTheme.colorScheme.surface)
        .border(borderWidth, borderColor, shape),
    ) {
      val viewportHeight = maxHeight

      Row(modifier = Modifier.fillMaxSize()) {
        // Line-number gutter. Drawn at viewport size and offset by the shared scroll,
        // so only visible numbers are measured/drawn (cheap for large payloads).
        Canvas(
          modifier = Modifier
            .width(gutterWidth)
            .fillMaxHeight(),
        ) {
          val layout = textLayout
          val padTop = vPad.toPx()
          val scroll = scrollState.value.toFloat()
          val rightPad = 8.dp.toPx()
          for (k in lineStarts.indices) {
            val top = if (layout != null) {
              val visual = layout.getLineForOffset(lineStarts[k].coerceIn(0, contentLength))
              padTop + layout.getLineTop(visual) - scroll
            } else {
              padTop + k * lineHeightPx - scroll
            }
            if (top > size.height || top + lineHeightPx < 0f) continue
            val measured = textMeasurer.measure(AnnotatedString((k + 1).toString()), gutterStyle)
            drawText(
              textLayoutResult = measured,
              topLeft = Offset(size.width - measured.size.width - rightPad, top),
            )
          }
        }

        Box(
          modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(dividerColor),
        )

        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
          // Current-line band, drawn behind the text and offset by the shared scroll.
          Canvas(modifier = Modifier.matchParentSize()) {
            val layout = textLayout
            if (!isFocused || layout == null) return@Canvas
            val cursor = cursorOffset.coerceIn(0, contentLength)
            val k = lineIndexForOffset(lineStarts, cursor)
            val lineStart = lineStarts[k]
            val lineEnd = if (k + 1 < lineStarts.size) {
              (lineStarts[k + 1] - 1).coerceAtLeast(lineStart)
            } else {
              contentLength
            }
            val startVisual = layout.getLineForOffset(lineStart.coerceIn(0, contentLength))
            val endVisual = layout.getLineForOffset(lineEnd.coerceIn(0, contentLength))
            val top = vPad.toPx() + layout.getLineTop(startVisual) - scrollState.value
            val bottom = vPad.toPx() + layout.getLineBottom(endVisual) - scrollState.value
            drawRect(
              color = bandColor,
              topLeft = Offset(0f, top),
              size = Size(size.width, bottom - top),
            )
          }

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
            onTextLayout = { textLayout = it },
            modifier = Modifier
              .fillMaxWidth()
              .verticalScroll(scrollState)
              .defaultMinSize(minHeight = viewportHeight) // make the whole area clickable
              .onFocusChanged { isFocused = it.isFocused }
              .padding(start = 4.dp, top = vPad, bottom = vPad, end = 8.dp),
            textStyle = editorStyle,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
          )
        }
      }
    }

    VerticalScrollbar(
      modifier = Modifier
        .width(4.dp)
        .align(Alignment.CenterEnd)
        .fillMaxHeight()
        .padding(vertical = 4.dp),
      adapter = rememberScrollbarAdapter(scrollState),
      style = LocalScrollbarStyle.current.copy(
        unhoverColor = MaterialTheme.colorScheme.outlineVariant,
        hoverColor = MaterialTheme.colorScheme.primary,
      ),
    )

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

/** Character offset where each logical line starts; size == logical line count. */
private fun lineStartOffsets(text: String): List<Int> = buildList {
  add(0)
  text.forEachIndexed { i, c -> if (c == '\n') add(i + 1) }
}

/** Index of the logical line containing [offset], given precomputed line start offsets. */
private fun lineIndexForOffset(starts: List<Int>, offset: Int): Int {
  var idx = 0
  for (i in starts.indices) {
    if (starts[i] <= offset) idx = i else break
  }
  return idx
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
fun RawLanguage.localizedShortLabel(): String = when (this) {
  RawLanguage.JSON -> stringResource(Res.string.lang_json)
  RawLanguage.XML -> stringResource(Res.string.lang_xml)
  RawLanguage.TEXT -> stringResource(Res.string.lang_text)
  RawLanguage.HTML -> stringResource(Res.string.lang_html)
  RawLanguage.JAVASCRIPT -> stringResource(Res.string.lang_javascript)
}
