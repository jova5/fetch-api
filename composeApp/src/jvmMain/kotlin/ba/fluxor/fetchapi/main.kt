package ba.fluxor.fetchapi

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ba.fluxor.fetchapi.configuration.DatabaseConnectionProvider
import ba.fluxor.fetchapi.configuration.DatabaseMigrator
import ba.fluxor.fetchapi.di.appModule
import org.koin.core.context.startKoin

fun main() {

  DatabaseConnectionProvider.init()
  DatabaseMigrator.migrate()

  startKoin {
    modules(appModule)
  }

  application {

    val windowState = rememberWindowState()

    Window(
      onCloseRequest = {
        DatabaseConnectionProvider.close()
        exitApplication()
      },
      state = windowState,
      title = "FetchAPI",
    ) {
      App()
    }
  }
}
