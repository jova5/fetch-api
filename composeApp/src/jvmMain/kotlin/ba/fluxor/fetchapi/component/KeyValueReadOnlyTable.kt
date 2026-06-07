package ba.fluxor.fetchapi.component

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun KeyValueReadOnlyTable(
  rows: List<Pair<String, String>>,
  keyLabel: String,
  valueLabel: String,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier) {
    val scrollState = rememberScrollState()

    SelectionContainer {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(scrollState),
      ) {
        HeaderRow(keyLabel = keyLabel, valueLabel = valueLabel)
        rows.forEach { (key, value) ->
          DataRow(key = key, value = value)
        }
      }
    }

    VerticalScrollbar(
      modifier = Modifier.width(4.dp)
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

@Composable
private fun HeaderRow(keyLabel: String, valueLabel: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 6.dp),
  ) {
    Text(
      text = keyLabel,
      style = MaterialTheme.typography.bodyLarge,
      modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.secondary,
    )
    Text(
      text = valueLabel,
      style = MaterialTheme.typography.bodyLarge,
      modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.secondary,
    )
  }
}

@Composable
private fun DataRow(key: String, value: String) {
  Row(
    verticalAlignment = Alignment.Top,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 4.dp),
  ) {
    Text(
      text = key,
      style = MaterialTheme.typography.bodySmall,
      modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
      color = MaterialTheme.colorScheme.onSurface,
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}
