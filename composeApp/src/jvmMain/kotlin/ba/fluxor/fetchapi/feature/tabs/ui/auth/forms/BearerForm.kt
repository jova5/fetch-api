package ba.fluxor.fetchapi.feature.tabs.ui.auth.forms

import androidx.compose.runtime.Composable
import ba.fluxor.fetchapi.feature.sub_project.data.auth.Auth
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.token
import org.jetbrains.compose.resources.stringResource

@Composable
fun BearerForm(auth: Auth.Bearer, emit: (Auth) -> Unit) {
  FieldText(
    label = stringResource(Res.string.token),
    value = auth.token,
    masked = true,
    onChange = { emit(auth.copy(token = it)) },
  )
}
