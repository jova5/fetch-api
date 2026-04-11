package ba.fluxor.fetchapi

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ba.fluxor.fetchapi.feature.project.ui.ProjectScreen

@Composable
@Preview
fun App() {
  MaterialTheme {
    ProjectScreen()
  }
}
