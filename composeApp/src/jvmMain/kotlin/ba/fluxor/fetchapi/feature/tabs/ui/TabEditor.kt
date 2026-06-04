package ba.fluxor.fetchapi.feature.tabs.ui

import androidx.compose.runtime.Composable
import ba.fluxor.fetchapi.feature.tabs.viewmodel.TabBuffer
import ba.fluxor.fetchapi.feature.tabs.viewmodel.TabItem

@Composable
fun TabEditor(
  tab: TabItem,
  onChange: (TabBuffer) -> Unit,
  onSave: () -> Unit,
  onSend: () -> Unit,
) {
  when (val buffer = tab.buffer) {
    is TabBuffer.SubProject -> SubProjectTabEditor(
      buffer = buffer,
      isDirty = tab.isDirty,
      onChange = onChange,
      onSave = onSave,
    )
    is TabBuffer.Folder -> FolderTabEditor(
      buffer = buffer,
      isDirty = tab.isDirty,
      onChange = onChange,
      onSave = onSave,
    )
    is TabBuffer.Request -> RequestTabEditor(
      buffer = buffer,
      isDirty = tab.isDirty,
      execution = tab.execution,
      onChange = onChange,
      onSave = onSave,
      onSend = onSend,
    )
  }
}
