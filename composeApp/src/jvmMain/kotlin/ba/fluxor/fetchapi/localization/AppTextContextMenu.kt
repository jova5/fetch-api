package ba.fluxor.fetchapi.localization

import androidx.compose.foundation.LocalContextMenuRepresentation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLocalization
import androidx.compose.ui.platform.PlatformLocalization
import ba.fluxor.fetchapi.ui.theme.ThemedContextMenuRepresentation
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.copy
import fetchapi.composeapp.generated.resources.cut
import fetchapi.composeapp.generated.resources.paste
import fetchapi.composeapp.generated.resources.select_all
import org.jetbrains.compose.resources.stringResource

/**
 * Provides a translated and theme-aware desktop text context menu (copy / cut / paste /
 * select all) to [content]. Must be composed inside the app theme and a locale scope so it
 * picks up the active colors and language.
 */
@Composable
fun AppTextContextMenu(content: @Composable () -> Unit) {

  val localization = object : PlatformLocalization {
    override val copy = stringResource(Res.string.copy)
    override val cut = stringResource(Res.string.cut)
    override val paste = stringResource(Res.string.paste)
    override val selectAll = stringResource(Res.string.select_all)
  }

  val representation = ThemedContextMenuRepresentation(
    backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    textColor = MaterialTheme.colorScheme.onSurface,
    hoverColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    borderColor = MaterialTheme.colorScheme.outlineVariant,
    shape = MaterialTheme.shapes.small,
  )

  CompositionLocalProvider(
    LocalLocalization provides localization,
    LocalContextMenuRepresentation provides representation,
    content = content,
  )
}
