package ba.fluxor.fetchapi.feature.tabs.viewmodel

import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.authorization
import fetchapi.composeapp.generated.resources.variables
import org.jetbrains.compose.resources.StringResource

enum class SubProjectTab {
  AUTHORIZATION,
  VARIABLES;

  fun labelRes(): StringResource = when (this) {
    AUTHORIZATION -> Res.string.authorization
    VARIABLES -> Res.string.variables
  }
}
