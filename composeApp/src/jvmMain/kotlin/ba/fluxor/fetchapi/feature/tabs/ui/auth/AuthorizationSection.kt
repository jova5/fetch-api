package ba.fluxor.fetchapi.feature.tabs.ui.auth

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.component.SquareOutlineButton
import ba.fluxor.fetchapi.feature.sub_project.data.auth.SubProjectAuth
import ba.fluxor.fetchapi.feature.sub_project.data.auth.SubProjectAuthCodec
import ba.fluxor.fetchapi.feature.sub_project.data.auth.SubProjectAuthTypes
import fetchapi.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun AuthorizationSection(
  authType: String,
  authConfig: String?,
  onChange: (authType: String, authConfig: String?) -> Unit,
) {
  val auth = remember(authType, authConfig) { SubProjectAuthCodec.decode(authType, authConfig) }

  fun emit(newAuth: SubProjectAuth) {
    val (type, config) = SubProjectAuthCodec.encode(newAuth)
    onChange(type, config)
  }

  Column(modifier = Modifier.fillMaxWidth()) {
    AuthTypeDropdown(
      selected = authType.ifBlank { SubProjectAuthTypes.NONE },
      onSelect = { newType ->
        if (newType != authType) {
          val (type, config) = SubProjectAuthCodec.encode(SubProjectAuthCodec.decode(newType, null))
          onChange(type, config)
        }
      },
    )

    Spacer(Modifier.height(16.dp))

    when (auth) {
      SubProjectAuth.None -> NoneEmptyState()
      is SubProjectAuth.Basic -> BasicAuthForm(auth, ::emit)
      is SubProjectAuth.Bearer -> BearerForm(auth, ::emit)
      is SubProjectAuth.ApiKey -> ApiKeyForm(auth, ::emit)
      is SubProjectAuth.Jwt -> JwtForm(auth, ::emit)
      is SubProjectAuth.Digest -> DigestForm(auth, ::emit)
      is SubProjectAuth.OAuth1 -> OAuth1Form(auth, ::emit)
      is SubProjectAuth.OAuth2 -> OAuth2Form(auth, ::emit)
      is SubProjectAuth.Custom -> CustomForm(auth, ::emit)
    }
  }
}

@Composable
private fun NoneEmptyState() {
  Text(
    text = stringResource(Res.string.auth_none_description),
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
  )
}

@Composable
private fun BasicAuthForm(auth: SubProjectAuth.Basic, emit: (SubProjectAuth) -> Unit) {
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

@Composable
private fun BearerForm(auth: SubProjectAuth.Bearer, emit: (SubProjectAuth) -> Unit) {
  FieldText(
    label = stringResource(Res.string.token),
    value = auth.token,
    onChange = { emit(auth.copy(token = it)) },
  )
}

@Composable
private fun ApiKeyForm(auth: SubProjectAuth.ApiKey, emit: (SubProjectAuth) -> Unit) {
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

@Composable
private fun JwtForm(auth: SubProjectAuth.Jwt, emit: (SubProjectAuth) -> Unit) {
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
  if (auth.addTo == SubProjectAuth.AddTo.QUERY) {
    FieldText(
      label = stringResource(Res.string.query_param_key),
      value = auth.queryParamKey,
      onChange = { emit(auth.copy(queryParamKey = it)) },
    )
  }
}

@Composable
private fun DigestForm(auth: SubProjectAuth.Digest, emit: (SubProjectAuth) -> Unit) {
  FieldText(stringResource(Res.string.username), auth.username) { emit(auth.copy(username = it)) }
  FieldText(stringResource(Res.string.password), auth.password) { emit(auth.copy(password = it)) }
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

@Composable
private fun OAuth1Form(auth: SubProjectAuth.OAuth1, emit: (SubProjectAuth) -> Unit) {
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

@Composable
private fun OAuth2Form(auth: SubProjectAuth.OAuth2, emit: (SubProjectAuth) -> Unit) {
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
private fun CustomForm(auth: SubProjectAuth.Custom, emit: (SubProjectAuth) -> Unit) {
  FieldText(
    label = stringResource(Res.string.custom_auth_raw),
    value = auth.raw,
    onChange = { emit(auth.copy(raw = it)) },
    singleLine = false,
    minHeight = 160.dp,
  )
}

@Composable
private fun FieldText(
  label: String,
  value: String,
  singleLine: Boolean = true,
  minHeight: androidx.compose.ui.unit.Dp = 0.dp,
  onChange: (String) -> Unit,
) {
  Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
    OutlinedTextField(
      value = value,
      onValueChange = onChange,
      label = { Text(label) },
      singleLine = singleLine,
      modifier = if (minHeight > 0.dp) Modifier.fillMaxWidth().height(minHeight) else Modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun CheckboxRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
  ) {
    Checkbox(checked = checked, onCheckedChange = onChange)
    Spacer(Modifier.width(4.dp))
    Text(label)
  }
}

@Composable
private fun AddToDropdown(selected: SubProjectAuth.AddTo, onSelect: (SubProjectAuth.AddTo) -> Unit) {
  EnumDropdown(
    label = stringResource(Res.string.add_to),
    options = listOf(SubProjectAuth.AddTo.HEADER, SubProjectAuth.AddTo.QUERY),
    selected = selected,
    onSelect = onSelect,
    optionLabel = { addToLabel(it) },
  )
}

@Composable
private fun addToLabel(value: SubProjectAuth.AddTo): String = when (value) {
  SubProjectAuth.AddTo.HEADER -> stringResource(Res.string.add_to_header)
  SubProjectAuth.AddTo.QUERY -> stringResource(Res.string.add_to_query)
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

private val jwtAlgorithms = listOf("HS256", "HS384", "HS512", "RS256", "RS384", "RS512", "ES256", "ES384", "ES512", "PS256", "PS384", "PS512")
private val digestAlgorithms = listOf("MD5", "MD5-sess", "SHA-256", "SHA-256-sess", "SHA-512-256", "SHA-512-256-sess")
private val oauth1SignatureMethods = listOf("HMAC-SHA1", "HMAC-SHA256", "HMAC-SHA512", "RSA-SHA1", "PLAINTEXT")
private val oauth2GrantTypes = listOf("AUTHORIZATION_CODE", "IMPLICIT", "PASSWORD", "CLIENT_CREDENTIALS")
private val oauth2ClientAuth = listOf("BODY", "HEADER")

@Composable
private fun AuthTypeDropdown(selected: String, onSelect: (String) -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  var buttonWidth by remember { mutableStateOf(0.dp) }
  val density = LocalDensity.current

  Text(text = stringResource(Res.string.auth_type))
  Box {
    SquareOutlineButton(
      text = selected,
      onClick = { expanded = true },
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth()
        .onGloballyPositioned { coordinates ->
          buttonWidth = with(density) { coordinates.size.width.toDp() + 30.dp }
        },
      contentAlignment = Alignment.CenterStart,
    )
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier.width(buttonWidth),
    ) {
      Box(modifier = Modifier.height(300.dp).fillMaxWidth()) {
        val scrollState = rememberScrollState()
        Column(
          modifier = Modifier.fillMaxWidth().verticalScroll(scrollState),
        ) {
          SubProjectAuthTypes.ALL.forEach { type ->
            DropdownMenuItem(
              text = {
                Text(text = type, maxLines = 1, overflow = TextOverflow.Ellipsis)
              },
              onClick = {
                onSelect(type)
                expanded = false
              },
              leadingIcon = {
                if (selected == type) {
                  Icon(imageVector = Icons.Default.Check, contentDescription = null)
                }
              },
            )
          }
        }
        VerticalScrollbar(
          modifier = Modifier
            .width(4.dp)
            .align(Alignment.CenterEnd)
            .fillMaxHeight(),
          adapter = rememberScrollbarAdapter(scrollState = scrollState),
          style = LocalScrollbarStyle.current.copy(
            unhoverColor = MaterialTheme.colorScheme.outlineVariant,
            hoverColor = MaterialTheme.colorScheme.primary,
          ),
        )
      }
    }
  }
}

@Composable
private fun <T> EnumDropdown(
  label: String,
  options: List<T>,
  selected: T,
  onSelect: (T) -> Unit,
  optionLabel: @Composable (T) -> String,
) {
  var expanded by remember { mutableStateOf(false) }
  var buttonWidth by remember { mutableStateOf(0.dp) }
  val density = LocalDensity.current

  Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
    Text(text = label)
    Box {
      SquareOutlineButton(
        text = optionLabel(selected),
        onClick = { expanded = true },
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth()
          .onGloballyPositioned { coordinates ->
            buttonWidth = with(density) { coordinates.size.width.toDp() + 30.dp }
          },
        contentAlignment = Alignment.CenterStart,
      )
      DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.width(buttonWidth),
      ) {
        options.forEach { option ->
          DropdownMenuItem(
            text = { Text(text = optionLabel(option), maxLines = 1, overflow = TextOverflow.Ellipsis) },
            onClick = {
              onSelect(option)
              expanded = false
            },
            leadingIcon = {
              if (option == selected) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
              }
            },
          )
        }
      }
    }
  }
}
