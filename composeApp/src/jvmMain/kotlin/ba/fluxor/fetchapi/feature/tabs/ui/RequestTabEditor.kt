package ba.fluxor.fetchapi.feature.tabs.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ba.fluxor.fetchapi.LocalWindowHeight
import ba.fluxor.fetchapi.component.*
import ba.fluxor.fetchapi.feature.request.data.RequestHeaderDerivation
import ba.fluxor.fetchapi.feature.settings.viewmodel.SettingsViewModel
import ba.fluxor.fetchapi.feature.tabs.ui.request.*
import ba.fluxor.fetchapi.feature.tabs.viewmodel.RequestTab
import ba.fluxor.fetchapi.feature.tabs.viewmodel.TabBuffer
import ba.fluxor.fetchapi.network.http.HttpMethod
import ba.fluxor.fetchapi.ui.theme.ThemeMode
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.save
import fetchapi.composeapp.generated.resources.send
import fetchapi.composeapp.generated.resources.url
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import java.awt.Cursor

@Composable
fun RequestTabEditor(
  buffer: TabBuffer.Request,
  isDirty: Boolean,
  onChange: (TabBuffer) -> Unit,
  onSave: () -> Unit,
) {
  var selectedTab by remember { mutableStateOf(RequestTab.entries.first()) }
  var autoHidden by remember { mutableStateOf(false) }
  val settingsVm: SettingsViewModel = koinViewModel()
  val state by settingsVm.state.collectAsStateWithLifecycle()
  val isDark = when (state.themeMode) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
  }

  Column(modifier = Modifier
    .fillMaxSize()
    .padding(16.dp)
  ) {

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
      CompactInput(
        value = buffer.name,
        onValueChange = { onChange(buffer.copy(name = it)) },
      )
      Spacer(Modifier.weight(1f))
      SquareButton(
        text = stringResource(Res.string.save),
        onClick = onSave,
        enabled = isDirty,
        modifier = Modifier
          .padding(horizontal = 16.dp, vertical = 6.dp),
        containerColor = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary
      )
    }
    Spacer(Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
      SimpleDropdown(
        options = HttpMethod.entries.map { it.name },
        selected = buffer.method,
        onSelect = { onChange(buffer.copy(method = it)) },
        width = 100.dp,
        optionLabel = { it },
      )
      Spacer(Modifier.width(8.dp))
      CompactInput(
        value = buffer.url,
        onValueChange = { newUrl ->
          val newParams = UrlParamSync.mergeFromUrl(newUrl, buffer.params)
          onChange(buffer.copy(url = newUrl, params = newParams))
        },
        placeholder = stringResource(Res.string.url),
        modifier = Modifier.weight(1f),
      )
      Spacer(Modifier.width(8.dp))
      SquareButton(
        text = stringResource(Res.string.send),
        onClick = {},
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
      )
    }

    Spacer(Modifier.height(12.dp))
    Divider(thickness = 1.dp)
    Spacer(Modifier.height(12.dp))

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
      RequestTab.entries.forEach { tab ->
        SquareOutlineButton(
          text = stringResource(tab.labelRes()),
          onClick = { selectedTab = tab },
          modifier = Modifier.padding(horizontal = 16.dp),
          borderWidth = if (selectedTab == tab) 2.dp else 0.dp,
        )
      }
    }
    Spacer(Modifier.height(8.dp))
    Divider(thickness = 1.dp)
    Spacer(Modifier.height(16.dp))

    Box(modifier = Modifier
      .weight(1f)
      .fillMaxWidth()
    ) {
      when (selectedTab) {
        RequestTab.PARAMS -> ParamsSection(
          params = buffer.params,
          onChange = { newParams ->
            val newUrl = UrlParamSync.rebuildUrl(buffer.url, newParams)
            onChange(buffer.copy(params = newParams, url = newUrl))
          },
        )

        RequestTab.AUTHORIZATION -> RequestAuthorizationSection(
          authType = buffer.authType,
          authConfig = buffer.authConfig,
          onChange = { newType, newConfig ->
            onChange(buffer.copy(authType = newType, authConfig = newConfig))
          },
        )

        RequestTab.HEADERS -> {
          val resolvedAuth = RequestHeaderDerivation.resolvedAuth(
            buffer.authType,
            buffer.authConfig,
            buffer.parentAuthType,
            buffer.parentAuthConfig,
          )
          HeadersSection(
            buffer = buffer,
            resolvedAuthHeaders = RequestHeaderDerivation.authHeaders(resolvedAuth),
            autoHidden = autoHidden,
            onCustomHeadersChange = { newHeaders -> onChange(buffer.copy(headers = newHeaders)) },
            onReadOnlyToggle = { key, included ->
              val updated = if (included)
                buffer.excludedAutoHeaders - key
              else
                buffer.excludedAutoHeaders + key
              onChange(buffer.copy(excludedAutoHeaders = updated))
            },
            onToggleAutoHidden = { autoHidden = !autoHidden },
          )
        }

        RequestTab.BODY -> BodySection(
          body = buffer.body,
          onChange = { newBody -> onChange(buffer.copy(body = newBody)) },
        )
      }
    }

    Spacer(Modifier.height(16.dp))

    val settingState by settingsVm.state.collectAsStateWithLifecycle()
    val density = LocalDensity.current

    val windowHeight = LocalWindowHeight.current

    val minHeight = 225.dp
    val maxHeight = windowHeight * 0.60f

    var percentage by remember { mutableStateOf(settingState.requestDividerPercentage) }

    val height by remember(windowHeight, percentage) {
      derivedStateOf {
        (minHeight + (maxHeight - minHeight) * percentage)
          .coerceIn(minHeight, maxHeight)
      }
    }
    var virtualMouseY by remember { mutableStateOf(height) }

    val dragState = rememberDraggableState { delta ->
      val deltaDp = with(density) { delta.toDp() }

      virtualMouseY -= deltaDp

      if (virtualMouseY in minHeight..maxHeight) {
        val range = maxHeight - minHeight
        if (range > 0.dp) {
          percentage = ((virtualMouseY - minHeight) / range).coerceIn(0f, 1f)
        }
      }
    }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .pointerHoverIcon(PointerIcon(Cursor(Cursor.N_RESIZE_CURSOR)))
        .draggable(
          state = dragState,
          orientation = Orientation.Vertical,
          onDragStarted = {
            virtualMouseY = height
          },
          onDragStopped = {
            settingsVm.setRequestDividerPercentage(percentage)
          }
        )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .height(4.dp)
      ) { }
    }

    ResponseView(isDark, height)
  }
}

@Composable
fun ResponseView(isDark: Boolean = false, height: Dp) {

  val verticalScrollState = rememberScrollState()
  val horizontalScrollState = rememberScrollState()
  val rawResponse = "[{\n" +
      "    \"name\": \"Adeel Solangi\",\n" +
      "    \"language\": \"Sindhi\",\n" +
      "    \"id\": \"V59OF92YF627HFY0\",\n" +
      "    \"bio\": \"Donec lobortis eleifend condimentum. Cras dictum dolor lacinia lectus vehicula rutrum. Maecenas quis nisi nunc. Nam tristique feugiat est vitae mollis. Maecenas quis nisi nunc.\",\n" +
      "    \"version\": 6.1\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Afzal Ghaffar\",\n" +
      "    \"language\": \"Sindhi\",\n" +
      "    \"id\": \"ENTOCR13RSCLZ6KU\",\n" +
      "    \"bio\": \"Aliquam sollicitudin ante ligula, eget malesuada nibh efficitur et. Pellentesque massa sem, scelerisque sit amet odio id, cursus tempor urna. Etiam congue dignissim volutpat. Vestibulum pharetra libero et velit gravida euismod.\",\n" +
      "    \"version\": 1.88\n" +
      "  },\n" +
      "  {\n" +
      "    \"name\": \"Aamir Solangi\",\n" +
      "    \"language\": \"Sindhi\",\n" +
      "    \"id\": \"IAKPO3R4761JDRVG\",\n" +
      "    \"bio\": \"Vestibulum pharetra libero et velit gravida euismod. Quisque mauris ligula, efficitur porttitor sodales ac, lacinia non ex. Fusce eu ultrices elit, vel posuere neque.\",\n" +
      "    \"version\": 7.27\n" +
      "  }" +
      "]"

  val formattedJson = remember(rawResponse, isDark) {
    formatAndHighlightJson(rawResponse, isDark)
  }

  SelectionContainer {
    Box(
      modifier = Modifier
        .height(height)
        .fillMaxWidth()
        .border(1.dp, MaterialTheme.colorScheme.outline, shape = MaterialTheme.shapes.medium)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(verticalScrollState)
          .horizontalScroll(horizontalScrollState)
          .padding(12.dp)
          .padding(end = 12.dp, bottom = 12.dp)
      ) {
        Text(
          text = formattedJson,
          style = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        )
      }

      VerticalScrollbar(
        adapter = rememberScrollbarAdapter(verticalScrollState),
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .fillMaxHeight()
          .padding(end = 2.dp, top = 4.dp, bottom = 4.dp)
      )

      HorizontalScrollbar(
        adapter = rememberScrollbarAdapter(horizontalScrollState),
        modifier = Modifier
          .align(Alignment.BottomStart)
          .fillMaxWidth()
          .padding(bottom = 2.dp, start = 4.dp, end = 4.dp)
      )
    }
  }
}
