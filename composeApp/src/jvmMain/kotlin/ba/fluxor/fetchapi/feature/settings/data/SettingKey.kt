package ba.fluxor.fetchapi.feature.settings.data

enum class SettingKey(val tag: String) {
  THEME_MODE("theme_mode"),
  COLOR_SCHEME("color_scheme"),
  LANGUAGE("language"),
  FONT_SCALE("font_scale"),
  DIVIDER_PERCENTAGE("divider_percentage"),
  REQUEST_DIVIDER_PERCENTAGE("request_divider_percentage"),
  ACTIVE_PROJECT_ID("active_project_id"),
  LAST_FOCUSED_TAB_PREFIX("last_focused_tab_"),
}
