package ba.fluxor.fetchapi

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.window.application
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import ba.fluxor.fetchapi.configuration.DatabaseConnectionProvider
import ba.fluxor.fetchapi.configuration.DatabaseMigrator
import ba.fluxor.fetchapi.di.AppLifecycleOwner
import ba.fluxor.fetchapi.di.appModule
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.koin.core.context.startKoin

fun main() {

  DatabaseConnectionProvider.init()
  DatabaseMigrator.migrate()

  startKoin {
    modules(appModule)
  }

  application {

    val appOwner = remember { AppLifecycleOwner() }
    CompositionLocalProvider(
      LocalLifecycleOwner provides appOwner,
      LocalViewModelStoreOwner provides appOwner
    ) {
      IntUiTheme {
        App(onCloseRequest = {
          DatabaseConnectionProvider.close()
          exitApplication()
        })
      }
    }
  }
}

