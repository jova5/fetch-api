package ba.fluxor.fetchapi.feature.settings.data

import ba.fluxor.fetchapi.localization.AppLanguage
import ba.fluxor.fetchapi.ui.theme.AppColorScheme
import ba.fluxor.fetchapi.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingRepository(private val dao: SettingDao) {

  suspend fun getThemeMode(): ThemeMode? = withContext(Dispatchers.IO) {
    dao.get(SettingKey.THEME_MODE.tag)
      ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
  }

  suspend fun setThemeMode(mode: ThemeMode) = withContext(Dispatchers.IO) {
    dao.upsert(SettingKey.THEME_MODE.tag, mode.name)
  }

  suspend fun getColorScheme(): AppColorScheme? = withContext(Dispatchers.IO) {
    dao.get(SettingKey.COLOR_SCHEME.tag)
      ?.let { runCatching { AppColorScheme.valueOf(it) }.getOrNull() }
  }

  suspend fun setColorScheme(scheme: AppColorScheme) = withContext(Dispatchers.IO) {
    dao.upsert(SettingKey.COLOR_SCHEME.tag, scheme.name)
  }

  suspend fun getLanguage(): AppLanguage? = withContext(Dispatchers.IO) {
    dao.get(SettingKey.LANGUAGE.tag)
      ?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() }
  }

  suspend fun setLanguage(language: AppLanguage) = withContext(Dispatchers.IO) {
    dao.upsert(SettingKey.LANGUAGE.tag, language.name)
  }

  suspend fun getFontScale() : Float? = withContext(Dispatchers.IO) {
    dao.get(SettingKey.FONT_SCALE.tag)
      ?.let { runCatching { it.toFloat() }.getOrNull() }
  }

  suspend fun setFontScale(fontScale: Float) = withContext(Dispatchers.IO) {
    dao.upsert(SettingKey.FONT_SCALE.tag, fontScale.toString())
  }

  suspend fun getDividerPercentage(): Float? = withContext(Dispatchers.IO) {
    dao.get(SettingKey.DIVIDER_PERCENTAGE.tag)
      ?.let { runCatching { it.toFloat() }.getOrNull() }
  }

  suspend fun setDividerPercentage(dividerPercentage: Float) = withContext(Dispatchers.IO) {
    dao.upsert(SettingKey.DIVIDER_PERCENTAGE.tag, dividerPercentage.toString())
  }

  suspend fun getRequestDividerPercentage(): Float? = withContext(Dispatchers.IO) {
    dao.get(SettingKey.REQUEST_DIVIDER_PERCENTAGE.tag)
      ?.let { runCatching { it.toFloat() }.getOrNull() }
  }

  suspend fun setRequestDividerPercentage(dividerPercentage: Float) = withContext(Dispatchers.IO) {
    dao.upsert(SettingKey.REQUEST_DIVIDER_PERCENTAGE.tag, dividerPercentage.toString())
  }
}
