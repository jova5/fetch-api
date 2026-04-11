package ba.fluxor.fetchapi

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ba.fluxor.fetchapi.configuration.DatabaseConnectionProvider
import ba.fluxor.fetchapi.configuration.DatabaseMigrator
import ba.fluxor.fetchapi.di.appModule
import org.koin.core.context.startKoin
import java.awt.GraphicsEnvironment

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
      undecorated = true,
      title = "FetchAPI",
    ) {
      App(
        windowState = windowState,
        onMinimize = { windowState.isMinimized = true },
        onToggleMaximize = {

          if (windowState.size == getWorkAreaSize()) {
            windowState.size = DpSize(800.dp, 600.dp) // your default size
            windowState.position = WindowPosition(100.dp, 100.dp)
          } else {
            val workArea = getWorkAreaSize()
            windowState.size = workArea
            windowState.position = WindowPosition(0.dp, 0.dp)
          }

//          windowState.placement = if (windowState.placement == WindowPlacement.Maximized) {
//            WindowPlacement.Floating
//          } else {
//            WindowPlacement.Maximized
//          }
        },
        onClose = {
          DatabaseConnectionProvider.close()
          exitApplication()
        },
      )
    }
  }
}

fun getWorkAreaSize(): DpSize {
  val screenInsets = GraphicsEnvironment
    .getLocalGraphicsEnvironment()
    .maximumWindowBounds  // this excludes the taskbar
  return DpSize(screenInsets.width.dp, screenInsets.height.dp)
}
