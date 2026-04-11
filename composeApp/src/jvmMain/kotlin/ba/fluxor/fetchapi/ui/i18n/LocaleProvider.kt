package ba.fluxor.fetchapi.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ENGLISH }

@Composable
fun LocaleProvider(
  language: AppLanguage,
  content: @Composable () -> Unit,
) {

  Locale.setDefault(Locale.forLanguageTag(language.tag))

  key(language) {
    CompositionLocalProvider(LocalAppLanguage provides language) {
      content()
    }
  }
}
