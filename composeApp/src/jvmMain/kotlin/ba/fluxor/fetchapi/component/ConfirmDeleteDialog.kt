package ba.fluxor.fetchapi.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fetchapi.composeapp.generated.resources.Res
import fetchapi.composeapp.generated.resources.cancel
import fetchapi.composeapp.generated.resources.confirm_delete_message
import fetchapi.composeapp.generated.resources.confirm_delete_title
import fetchapi.composeapp.generated.resources.delete
import org.jetbrains.compose.resources.stringResource

@Composable
fun ConfirmDeleteDialog(
  entityName: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface {
      Column(
        modifier = Modifier.widthIn(max = 420.dp).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Row {
          Text(
            text = stringResource(Res.string.confirm_delete_title),
            style = MaterialTheme.typography.titleMedium,
          )
          Text(
            text = entityName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "?",
            style = MaterialTheme.typography.titleMedium,
          )
        }
        Text(
          text = stringResource(Res.string.confirm_delete_message),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
          modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
          SquareOutlineButton(
            text = stringResource(Res.string.cancel),
            onClick = onDismiss,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            borderWidth = 0.dp
          )
          SquareButton(
            text = stringResource(Res.string.delete),
            onClick = {
              onConfirm()
              onDismiss()
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
          )
        }
      }
    }
  }
}
