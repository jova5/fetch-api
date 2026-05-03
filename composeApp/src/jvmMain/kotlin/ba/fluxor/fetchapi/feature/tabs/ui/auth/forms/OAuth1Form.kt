package ba.fluxor.fetchapi.feature.tabs.ui.auth.forms

import androidx.compose.runtime.Composable
import ba.fluxor.fetchapi.feature.sub_project.data.auth.SubProjectAuth
import fetchapi.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

private val oauth1SignatureMethods = listOf("HMAC-SHA1", "HMAC-SHA256", "HMAC-SHA512", "RSA-SHA1", "PLAINTEXT")

@Composable
fun OAuth1Form(auth: SubProjectAuth.OAuth1, emit: (SubProjectAuth) -> Unit) {
  FieldText(stringResource(Res.string.consumer_key), auth.consumerKey) { emit(auth.copy(consumerKey = it)) }
  FieldText(stringResource(Res.string.consumer_secret), auth.consumerSecret) { emit(auth.copy(consumerSecret = it)) }
  FieldText(stringResource(Res.string.access_token), auth.token) { emit(auth.copy(token = it)) }
  FieldText(stringResource(Res.string.token_secret), auth.tokenSecret) { emit(auth.copy(tokenSecret = it)) }
  EnumDropdown(
    label = stringResource(Res.string.signature_method),
    options = oauth1SignatureMethods,
    selected = auth.signatureMethod,
    onSelect = { emit(auth.copy(signatureMethod = it)) },
    optionLabel = { it },
  )
  FieldText(stringResource(Res.string.timestamp), auth.timestamp) { emit(auth.copy(timestamp = it)) }
  FieldText(stringResource(Res.string.nonce), auth.nonce) { emit(auth.copy(nonce = it)) }
  FieldText(stringResource(Res.string.version), auth.version) { emit(auth.copy(version = it)) }
  FieldText(stringResource(Res.string.realm), auth.realm) { emit(auth.copy(realm = it)) }
  FieldText(stringResource(Res.string.callback), auth.callback) { emit(auth.copy(callback = it)) }
  FieldText(stringResource(Res.string.verifier), auth.verifier) { emit(auth.copy(verifier = it)) }
  CheckboxRow(stringResource(Res.string.include_body_hash), auth.includeBodyHash) { emit(auth.copy(includeBodyHash = it)) }
  CheckboxRow(stringResource(Res.string.add_empty_params), auth.addEmptyParamsToSignature) {
    emit(auth.copy(addEmptyParamsToSignature = it))
  }
  AddToDropdown(auth.addTo) { emit(auth.copy(addTo = it)) }
}
