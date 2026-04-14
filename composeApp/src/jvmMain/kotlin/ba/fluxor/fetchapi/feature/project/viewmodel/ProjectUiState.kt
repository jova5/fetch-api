package ba.fluxor.fetchapi.feature.project.viewmodel

import ba.fluxor.fetchapi.feature.project.data.Project

data class ProjectUiState(
  val projects: List<Project> = emptyList(),
  val selected: Project? = null,
  val active: Project? = null,
  val isLoading: Boolean = false,
  val error: String? = null,
)
