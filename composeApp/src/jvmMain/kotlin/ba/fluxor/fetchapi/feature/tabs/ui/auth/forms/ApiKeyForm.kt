package ba.fluxor.fetchapi.feature.tabs.ui.auth.forms

import androidx.compose.runtime.Composable
import ba.fluxor.fetchapi.feature.sub_project.data.auth.Auth
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.api_key_key
import fetchapi.composeapp.generated.resources.api_key_value
import org.jetbrains.compose.resources.stringResource

@Composable
fun ApiKeyForm(auth: Auth.ApiKey, emit: (Auth) -> Unit) {
  FieldText(
    label = stringResource(Res.string.api_key_key),
    value = auth.key,
    onChange = { emit(auth.copy(key = it)) },
  )
  FieldText(
    label = stringResource(Res.string.api_key_value),
    value = auth.value,
    onChange = { emit(auth.copy(value = it)) },
  )
  AddToDropdown(
    selected = auth.addTo,
    onSelect = { emit(auth.copy(addTo = it)) },
  )
}
