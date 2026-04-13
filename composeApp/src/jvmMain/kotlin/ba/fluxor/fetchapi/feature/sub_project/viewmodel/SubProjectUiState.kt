package ba.fluxor.fetchapi.feature.sub_project.viewmodel

import ba.fluxor.fetchapi.feature.sub_project.data.SubProject

data class SubProjectUiState(
  val showSubProjectDialog: Boolean = false,
  val editingSubProject: SubProject? = null,
  val error: String? = null,
  val isLoading: Boolean = false
  )
