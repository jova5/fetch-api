package ba.fluxor.fetchapi.feature.tabs.ui.request

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.component.SquareOutlineButton
import ba.fluxor.fetchapi.feature.sub_project.data.auth.Auth
import ba.fluxor.fetchapi.feature.sub_project.data.auth.AuthCodec
import ba.fluxor.fetchapi.feature.sub_project.data.auth.AuthTypes
import ba.fluxor.fetchapi.feature.tabs.ui.auth.forms.ApiKeyForm
import ba.fluxor.fetchapi.feature.tabs.ui.auth.forms.BasicAuthForm
import ba.fluxor.fetchapi.feature.tabs.ui.auth.forms.BearerForm
import ba.fluxor.fetchapi.feature.tabs.ui.auth.forms.CustomForm
import ba.fluxor.fetchapi.feature.tabs.ui.auth.forms.DigestForm
import ba.fluxor.fetchapi.feature.tabs.ui.auth.forms.JwtForm
import ba.fluxor.fetchapi.feature.tabs.ui.auth.forms.OAuth1Form
import ba.fluxor.fetchapi.feature.tabs.ui.auth.forms.OAuth2Form
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.auth_inherit_description
import fetchapi.composeapp.generated.resources.auth_none_description
import fetchapi.composeapp.generated.resources.auth_type
import fetchapi.composeapp.generated.resources.inherit_from_parent
import org.jetbrains.compose.resources.stringResource

private const val INHERIT = "INHERIT"

@Composable
fun RequestAuthorizationSection(
  authType: String,
  authConfig: String?,
  onChange: (authType: String, authConfig: String?) -> Unit,
) {
  val effectiveType = authType.ifBlank { INHERIT }

  fun emit(newAuth: Auth) {
    val (type, config) = AuthCodec.encode(newAuth)
    onChange(type, config)
  }

  Column(modifier = Modifier.widthIn(max = 800.dp)) {
    RequestAuthTypeDropdown(
      selected = effectiveType,
      onSelect = { newType ->
        if (newType == effectiveType) return@RequestAuthTypeDropdown
        when (newType) {
          INHERIT -> onChange(INHERIT, null)
          else -> {
            val (type, config) = AuthCodec.encode(AuthCodec.decode(newType, null))
            onChange(type, config)
          }
        }
      },
    )

    Spacer(Modifier.height(32.dp))

    if (effectiveType == INHERIT) {
      Text(
        text = stringResource(Res.string.auth_inherit_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      return
    }

    val auth = remember(authType, authConfig) { AuthCodec.decode(authType, authConfig) }

    Box {
      val scrollState = rememberScrollState()
      Column(modifier = Modifier.verticalScroll(scrollState).padding(end = 16.dp)) {
        when (auth) {
          Auth.None -> Text(
            text = stringResource(Res.string.auth_none_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
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
        modifier = Modifier.width(4.dp).align(Alignment.CenterEnd).fillMaxHeight(),
        adapter = rememberScrollbarAdapter(scrollState = scrollState),
        style = LocalScrollbarStyle.current.copy(
          unhoverColor = MaterialTheme.colorScheme.outlineVariant,
          hoverColor = MaterialTheme.colorScheme.primary,
        ),
      )
    }
  }
}

@Composable
private fun RequestAuthTypeDropdown(selected: String, onSelect: (String) -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  var buttonWidth by remember { mutableStateOf(0.dp) }
  val density = LocalDensity.current

  val options = listOf(INHERIT) + AuthTypes.ALL
  val inheritLabel = stringResource(Res.string.inherit_from_parent)
  val displayLabel = if (selected == INHERIT) inheritLabel else selected

  Text(
    text = stringResource(Res.string.auth_type),
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.secondary,
  )
  Spacer(modifier = Modifier.size(4.dp))
  Box(modifier = Modifier.padding(end = 16.dp)) {
    SquareOutlineButton(
      text = displayLabel,
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
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)) {
          options.forEach { option ->
            val label = if (option == INHERIT) inheritLabel else option
            DropdownMenuItem(
              text = { Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
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
        VerticalScrollbar(
          modifier = Modifier.width(4.dp).align(Alignment.CenterEnd).fillMaxHeight(),
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
