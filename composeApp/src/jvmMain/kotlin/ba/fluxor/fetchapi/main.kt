package ba.fluxor.fetchapi

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ba.fluxor.fetchapi.configuration.DatabaseMigrator

fun main() {

  DatabaseMigrator.migrate()

  application {
    Window(
      onCloseRequest = ::exitApplication,
      title = "FetchAPI",
    ) {
      App()
    }
  }
}
