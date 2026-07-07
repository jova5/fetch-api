package ba.fluxor.fetchapi.feature.tabs.ui.auth.forms

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.feature.sub_project.data.auth.Auth
import fetchapi.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

private val jwtAlgorithms = listOf("HS256", "HS384", "HS512", "RS256", "RS384", "RS512", "ES256", "ES384", "ES512", "PS256", "PS384", "PS512")

@Composable
fun JwtForm(auth: Auth.Jwt, emit: (Auth) -> Unit) {
  EnumDropdown(
    label = stringResource(Res.string.algorithm),
    options = jwtAlgorithms,
    selected = auth.algorithm,
    onSelect = { emit(auth.copy(algorithm = it)) },
    optionLabel = { it },
  )
  FieldText(
    label = stringResource(Res.string.secret),
    value = auth.secret,
    masked = true,
    onChange = { emit(auth.copy(secret = it)) },
  )
  CheckboxRow(
    label = stringResource(Res.string.secret_base64),
    checked = auth.secretBase64Encoded,
    onChange = { emit(auth.copy(secretBase64Encoded = it)) },
  )
  FieldText(
    label = stringResource(Res.string.payload),
    value = auth.payload,
    onChange = { emit(auth.copy(payload = it)) },
    singleLine = false,
    minHeight = 100.dp,
  )
  FieldText(
    label = stringResource(Res.string.header_prefix),
    value = auth.headerPrefix,
    onChange = { emit(auth.copy(headerPrefix = it)) },
  )
  AddToDropdown(
    selected = auth.addTo,
    onSelect = { emit(auth.copy(addTo = it)) },
  )
  if (auth.addTo == Auth.AddTo.QUERY) {
    FieldText(
      label = stringResource(Res.string.query_param_key),
      value = auth.queryParamKey,
      onChange = { emit(auth.copy(queryParamKey = it)) },
    )
  }
}
