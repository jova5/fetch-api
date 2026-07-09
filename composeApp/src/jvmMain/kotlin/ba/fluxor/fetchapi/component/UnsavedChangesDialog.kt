package ba.fluxor.fetchapi.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fetchapi.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/** Modal shown when closing a tab that has unsaved changes: Save / Discard / Cancel. */
@Composable
fun UnsavedChangesDialog(
  entityName: String,
  onSave: () -> Unit,
  onDiscard: () -> Unit,
  onCancel: () -> Unit,
) {
  Dialog(onDismissRequest = onCancel) {
    Surface {
      Column(
        modifier = Modifier.widthIn(max = 420.dp).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = stringResource(Res.string.unsaved_changes_title),
          style = MaterialTheme.typography.titleMedium,
        )
        Row (
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = entityName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = stringResource(Res.string.unsaved_changes_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          SquareOutlineButton(
            text = stringResource(Res.string.discard),
            onClick = onDiscard,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            borderWidth = 0.dp,
            contentColor = MaterialTheme.colorScheme.error,
          )
          Spacer(modifier = Modifier.weight(1f))
          SquareOutlineButton(
            text = stringResource(Res.string.cancel),
            onClick = onCancel,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
          )
          SquareButton(
            text = stringResource(Res.string.save),
            onClick = onSave,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
          )
        }
      }
    }
  }
}
