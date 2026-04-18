package ba.fluxor.fetchapi.feature.sub_project.viewmodel

import org.jetbrains.compose.resources.StringResource

data class SubProjectUiState(
  val error: StringResource? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = false,
)
