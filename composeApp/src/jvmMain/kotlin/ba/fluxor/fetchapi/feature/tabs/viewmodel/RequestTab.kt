package ba.fluxor.fetchapi.feature.tabs.viewmodel

import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.authorization
import fetchapi.composeapp.generated.resources.body
import fetchapi.composeapp.generated.resources.headers
import fetchapi.composeapp.generated.resources.params
import org.jetbrains.compose.resources.StringResource

enum class RequestTab {
  PARAMS,
  AUTHORIZATION,
  HEADERS,
  BODY;

  fun labelRes(): StringResource = when (this) {
    PARAMS -> Res.string.params
    AUTHORIZATION -> Res.string.authorization
    HEADERS -> Res.string.headers
    BODY -> Res.string.body
  }
}
