package ba.fluxor.fetchapi.feature.tabs.ui.auth.forms

import androidx.compose.runtime.Composable
import ba.fluxor.fetchapi.feature.sub_project.data.auth.SubProjectAuth
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.password
import fetchapi.composeapp.generated.resources.username
import org.jetbrains.compose.resources.stringResource

@Composable
fun BasicAuthForm(auth: SubProjectAuth.Basic, emit: (SubProjectAuth) -> Unit) {
  FieldText(
    label = stringResource(Res.string.username),
    value = auth.username,
    onChange = { emit(auth.copy(username = it)) },
  )
  FieldText(
    label = stringResource(Res.string.password),
    value = auth.password,
    onChange = { emit(auth.copy(password = it)) },
  )
}
