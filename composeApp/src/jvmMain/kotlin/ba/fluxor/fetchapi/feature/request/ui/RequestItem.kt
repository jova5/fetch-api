package ba.fluxor.fetchapi.feature.request.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.component.TooltipBelow
import ba.fluxor.fetchapi.feature.request.data.Request
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.delete
import fetchapi.composeapp.generated.resources.edit
import fetchapi.composeapp.generated.resources.menu
import fetchapi.composeapp.generated.resources.more_actions
import org.jetbrains.compose.resources.stringResource

@Composable
fun RequestItem(
  isHovered: Boolean,
  request: Request,
  indent: Dp,
  onDropdownOpen: () -> Unit,
  onDropdownClose: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  var showMenu by remember { mutableStateOf(false) }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(4.dp))
      .clickable(onClick = onEdit)
      .padding(start = indent, top = 4.dp, bottom = 4.dp, end = 2.dp),
  ) {
    Text(
      text = shortMethod(request.method),
      color = methodColor(request.method),
      fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.70,
    )
    Spacer(modifier = Modifier.size(4.dp))
    Text(
      text = request.name,
      style = MaterialTheme.typography.bodySmall,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier
        .padding(top = 4.dp, bottom = 4.dp)
        .weight(1f),
    )
    Box {
      if (isHovered) {
        TooltipBelow(
          text = stringResource(Res.string.more_actions)
        ) {
          IconButton(
            onClick = {
              showMenu = true
              onDropdownOpen()
            },
            modifier = Modifier.size(20.dp)
          ) {
            Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = stringResource(Res.string.menu),
              modifier = Modifier.size(16.dp))
          }
        }
      }
      DropdownMenu(
        expanded = showMenu,
        onDismissRequest = {
          showMenu = false
          onDropdownClose()
        }
      ) {
        DropdownMenuItem(
          text = { Text(stringResource(Res.string.edit)) },
          onClick = { showMenu = false; onEdit() }
        )
        DropdownMenuItem(
          text = { Text(stringResource(Res.string.delete)) },
          onClick = { showMenu = false; onDelete() }
        )
      }
    }
  }
}

@Composable
private fun methodColor(method: String): Color = when (method.uppercase()) {
  "GET" -> Color(0xFF4CAF50)
  "POST" -> Color(0xFFFF9800)
  "PUT" -> Color(0xFF2196F3)
  "PATCH" -> Color(0xFF9C27B0)
  "DELETE" -> Color(0xFFF44336)
  "HEAD" -> Color(0xFF607D8B)
  "OPTIONS" -> Color(0xFF795548)
  else -> MaterialTheme.colorScheme.onSurface
}

private fun shortMethod(method: String): String = when (method.uppercase()) {
  "GET" -> "GET"
  "POST" -> "POST"
  "PUT" -> "PUT"
  "PATCH" -> "PATCH"
  "DELETE" -> "DEL"
  "HEAD" -> "HEAD"
  "OPTIONS" -> "OPT"
  else -> "GET"
}
