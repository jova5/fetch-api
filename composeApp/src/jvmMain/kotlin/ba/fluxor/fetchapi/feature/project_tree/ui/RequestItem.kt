package ba.fluxor.fetchapi.feature.project_tree.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ba.fluxor.fetchapi.feature.request.data.Request

@Composable
fun RequestItem(
  request: Request,
  indent: Dp,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  var showMenu by remember { mutableStateOf(false) }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit).padding(start = indent, top = 2.dp, bottom = 2.dp),
  ) {
    Text(
      text = request.method,
      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
      color = methodColor(request.method),
      modifier = Modifier.width(40.dp),
    )
    Text(
      text = request.name,
      style = MaterialTheme.typography.bodySmall,
      modifier = Modifier.weight(1f),
    )
    Box {
      IconButton(onClick = { showMenu = true }, modifier = Modifier.size(20.dp)) {
        Icon(Icons.Default.MoreVert, contentDescription = "Menu", modifier = Modifier.size(16.dp))
      }
      DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
        DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEdit() })
        DropdownMenuItem(text = { Text("Delete") }, onClick = { showMenu = false; onDelete() })
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
