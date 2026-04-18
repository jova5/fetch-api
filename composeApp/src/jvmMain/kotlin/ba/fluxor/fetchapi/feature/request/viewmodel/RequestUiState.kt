package ba.fluxor.fetchapi.feature.request.viewmodel

import org.jetbrains.compose.resources.StringResource

data class RequestUiState(
  val error: StringResource? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = false,
)
