package ba.fluxor.fetchapi.feature.tabs.ui.auth.forms

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.feature.sub_project.data.auth.Auth
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.custom_auth_raw
import org.jetbrains.compose.resources.stringResource

@Composable
fun CustomForm(auth: Auth.Custom, emit: (Auth) -> Unit) {
  FieldText(
    label = stringResource(Res.string.custom_auth_raw),
    value = auth.raw,
    onChange = { emit(auth.copy(raw = it)) },
    singleLine = false,
    minHeight = 160.dp,
  )
}
