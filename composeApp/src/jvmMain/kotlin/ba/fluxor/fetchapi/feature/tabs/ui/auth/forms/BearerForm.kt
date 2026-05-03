package ba.fluxor.fetchapi.feature.tabs.ui.auth.forms

import androidx.compose.runtime.Composable
import ba.fluxor.fetchapi.feature.sub_project.data.auth.SubProjectAuth
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.token
import org.jetbrains.compose.resources.stringResource

@Composable
fun BearerForm(auth: SubProjectAuth.Bearer, emit: (SubProjectAuth) -> Unit) {
  FieldText(
    label = stringResource(Res.string.token),
    value = auth.token,
    onChange = { emit(auth.copy(token = it)) },
  )
}
