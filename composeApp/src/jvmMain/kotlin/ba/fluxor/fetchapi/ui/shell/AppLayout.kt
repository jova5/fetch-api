package ba.fluxor.fetchapi.ui.shell

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowState

@Composable
fun FrameWindowScope.AppLayout(
  windowState: WindowState,
  onMinimize: () -> Unit,
  onToggleMaximize: () -> Unit,
  onClose: () -> Unit,
) {
  Surface(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      TopBar(
        windowState = windowState,
        onMinimize = onMinimize,
        onToggleMaximize = onToggleMaximize,
        onClose = onClose,
      )
      MainArea(modifier = Modifier.fillMaxSize())
    }
  }
}
