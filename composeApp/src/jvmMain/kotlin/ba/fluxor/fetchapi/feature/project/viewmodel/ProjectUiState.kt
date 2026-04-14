package ba.fluxor.fetchapi.feature.project.viewmodel

import ba.fluxor.fetchapi.feature.project.data.Project
import org.jetbrains.compose.resources.StringResource

data class ProjectUiState(
  val error: StringResource? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = false,
  val projects: List<Project> = emptyList(),
  val selected: Project? = null,
  val active: Project? = null,
)
