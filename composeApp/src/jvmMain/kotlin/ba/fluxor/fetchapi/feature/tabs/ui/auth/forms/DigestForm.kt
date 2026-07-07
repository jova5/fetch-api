package ba.fluxor.fetchapi.feature.tabs.ui.auth.forms

import androidx.compose.runtime.Composable
import ba.fluxor.fetchapi.feature.sub_project.data.auth.Auth
import fetchapi.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

private val digestAlgorithms = listOf("MD5", "MD5-sess", "SHA-256", "SHA-256-sess", "SHA-512-256", "SHA-512-256-sess")

@Composable
fun DigestForm(auth: Auth.Digest, emit: (Auth) -> Unit) {
  FieldText(stringResource(Res.string.username), auth.username) { emit(auth.copy(username = it)) }
  FieldText(stringResource(Res.string.password), auth.password, masked = true) { emit(auth.copy(password = it)) }
  FieldText(stringResource(Res.string.realm), auth.realm) { emit(auth.copy(realm = it)) }
  FieldText(stringResource(Res.string.nonce), auth.nonce) { emit(auth.copy(nonce = it)) }
  EnumDropdown(
    label = stringResource(Res.string.algorithm),
    options = digestAlgorithms,
    selected = auth.algorithm,
    onSelect = { emit(auth.copy(algorithm = it)) },
    optionLabel = { it },
  )
  FieldText(stringResource(Res.string.qop), auth.qop) { emit(auth.copy(qop = it)) }
  FieldText(stringResource(Res.string.nonce_count), auth.nonceCount) { emit(auth.copy(nonceCount = it)) }
  FieldText(stringResource(Res.string.client_nonce), auth.clientNonce) { emit(auth.copy(clientNonce = it)) }
  FieldText(stringResource(Res.string.opaque), auth.opaque) { emit(auth.copy(opaque = it)) }
}
