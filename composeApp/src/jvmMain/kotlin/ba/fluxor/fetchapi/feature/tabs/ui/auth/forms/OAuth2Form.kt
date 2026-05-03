package ba.fluxor.fetchapi.feature.tabs.ui.auth.forms

import androidx.compose.runtime.Composable
import ba.fluxor.fetchapi.feature.sub_project.data.auth.SubProjectAuth
import fetchapi.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

private val oauth2GrantTypes = listOf("AUTHORIZATION_CODE", "IMPLICIT", "PASSWORD", "CLIENT_CREDENTIALS")
private val oauth2ClientAuth = listOf("BODY", "HEADER")

@Composable
fun OAuth2Form(auth: SubProjectAuth.OAuth2, emit: (SubProjectAuth) -> Unit) {
  FieldText(stringResource(Res.string.access_token), auth.accessToken) { emit(auth.copy(accessToken = it)) }
  FieldText(stringResource(Res.string.header_prefix), auth.headerPrefix) { emit(auth.copy(headerPrefix = it)) }
  EnumDropdown(
    label = stringResource(Res.string.grant_type),
    options = oauth2GrantTypes,
    selected = auth.grantType,
    onSelect = { emit(auth.copy(grantType = it)) },
    optionLabel = { grantTypeLabel(it) },
  )
  FieldText(stringResource(Res.string.callback), auth.callbackUrl) { emit(auth.copy(callbackUrl = it)) }
  FieldText(stringResource(Res.string.auth_url), auth.authUrl) { emit(auth.copy(authUrl = it)) }
  FieldText(stringResource(Res.string.access_token_url), auth.accessTokenUrl) { emit(auth.copy(accessTokenUrl = it)) }
  FieldText(stringResource(Res.string.client_id), auth.clientId) { emit(auth.copy(clientId = it)) }
  FieldText(stringResource(Res.string.client_secret), auth.clientSecret) { emit(auth.copy(clientSecret = it)) }
  FieldText(stringResource(Res.string.scope), auth.scope) { emit(auth.copy(scope = it)) }
  FieldText(stringResource(Res.string.state), auth.state) { emit(auth.copy(state = it)) }
  EnumDropdown(
    label = stringResource(Res.string.client_authentication),
    options = oauth2ClientAuth,
    selected = auth.clientAuthentication,
    onSelect = { emit(auth.copy(clientAuthentication = it)) },
    optionLabel = { clientAuthLabel(it) },
  )
  if (auth.grantType == "PASSWORD") {
    FieldText(stringResource(Res.string.username), auth.username) { emit(auth.copy(username = it)) }
    FieldText(stringResource(Res.string.password), auth.password) { emit(auth.copy(password = it)) }
  }
  AddToDropdown(auth.addTo) { emit(auth.copy(addTo = it)) }
}



@Composable
private fun grantTypeLabel(value: String): String = when (value) {
  "AUTHORIZATION_CODE" -> stringResource(Res.string.grant_authorization_code)
  "IMPLICIT" -> stringResource(Res.string.grant_implicit)
  "PASSWORD" -> stringResource(Res.string.grant_password)
  "CLIENT_CREDENTIALS" -> stringResource(Res.string.grant_client_credentials)
  else -> value
}

@Composable
private fun clientAuthLabel(value: String): String = when (value) {
  "BODY" -> stringResource(Res.string.client_auth_body)
  "HEADER" -> stringResource(Res.string.client_auth_header)
  else -> value
}
