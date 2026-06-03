package ba.fluxor.fetchapi.feature.tabs.ui.request.raw

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.component.SimpleDropdown
import ba.fluxor.fetchapi.component.SquareIconButton
import ba.fluxor.fetchapi.component.TooltipBelow
import ba.fluxor.fetchapi.feature.request.data.RawLanguage
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.body_raw_beautify
import org.jetbrains.compose.resources.stringResource

@Composable
fun RawToolbar(
  language: RawLanguage,
  onLanguageChange: (RawLanguage) -> Unit,
  beautifyEnabled: Boolean,
  onBeautify: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    SimpleDropdown(
      options = RawLanguage.entries,
      selected = language,
      onSelect = onLanguageChange,
      width = 100.dp,
      optionLabel = { it.localizedShortLabel() },
    )
    TooltipBelow(
      text = stringResource(Res.string.body_raw_beautify)
    ) {
      SquareIconButton(
        icon = Icons.Default.LocalFlorist,
        onClick = onBeautify,
        enabled = beautifyEnabled,
        borderWidth = 1.dp,
      )
    }
  }
}
