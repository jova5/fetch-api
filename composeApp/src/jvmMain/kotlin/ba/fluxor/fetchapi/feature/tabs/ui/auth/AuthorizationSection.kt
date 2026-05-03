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
import ba.fluxor.fetchapi.feature.sub_project.data.auth.Auth
import ba.fluxor.fetchapi.feature.sub_project.data.auth.AuthCodec
import ba.fluxor.fetchapi.feature.sub_project.data.auth.AuthTypes
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
  val auth = remember(authType, authConfig) { AuthCodec.decode(authType, authConfig) }

  fun emit(newAuth: Auth) {
    val (type, config) = AuthCodec.encode(newAuth)
    onChange(type, config)
  }

  Column(modifier = Modifier.fillMaxWidth()) {
    AuthTypeDropdown(
      selected = authType.ifBlank { AuthTypes.NONE },
      onSelect = { newType ->
        if (newType != authType) {
          val (type, config) = AuthCodec.encode(AuthCodec.decode(newType, null))
          onChange(type, config)
        }
      },
    )

    Spacer(Modifier.height(16.dp))

    Box {

      val scrollState = rememberScrollState()

      Column(modifier = Modifier.verticalScroll(scrollState)) {

        when (auth) {
          Auth.None -> NoneEmptyState()
          is Auth.Basic -> BasicAuthForm(auth, ::emit)
          is Auth.Bearer -> BearerForm(auth, ::emit)
          is Auth.ApiKey -> ApiKeyForm(auth, ::emit)
          is Auth.Jwt -> JwtForm(auth, ::emit)
          is Auth.Digest -> DigestForm(auth, ::emit)
          is Auth.OAuth1 -> OAuth1Form(auth, ::emit)
          is Auth.OAuth2 -> OAuth2Form(auth, ::emit)
          is Auth.Custom -> CustomForm(auth, ::emit)
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
