package ba.fluxor.fetchapi.feature.tabs.ui.auth

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.feature.sub_project.data.auth.SubProjectAuth
import ba.fluxor.fetchapi.feature.sub_project.data.auth.SubProjectAuthCodec
import ba.fluxor.fetchapi.feature.sub_project.data.auth.SubProjectAuthTypes
import ba.fluxor.fetchapi.feature.tabs.ui.auth.forms.*
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.auth_none_description
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

    Box {

      val scrollState = rememberScrollState()

      Column(modifier = Modifier.verticalScroll(scrollState)) {

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
      VerticalScrollbar(
        modifier = Modifier
          .width(4.dp)
          .align(Alignment.CenterEnd)
          .fillMaxHeight(),
        adapter = rememberScrollbarAdapter(scrollState = scrollState),
        style = LocalScrollbarStyle.current.copy(
          unhoverColor = MaterialTheme.colorScheme.outlineVariant,
          hoverColor = MaterialTheme.colorScheme.primary
        ),
      )
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
