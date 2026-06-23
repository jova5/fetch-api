package ba.fluxor.fetchapi.ui.util

import java.awt.FileDialog
import java.awt.Frame

/**
 * Opens the native OS file-open dialog and returns the absolute paths of the chosen files.
 * Returns an empty list when the user cancels. Blocking (modal) — call from a click handler.
 */
fun pickFiles(title: String = "Choose files", multiple: Boolean = true): List<String> {
  val dialog = FileDialog(null as Frame?, title, FileDialog.LOAD).apply {
    isMultipleMode = multiple
    isVisible = true
  }
  return dialog.files.map { it.absolutePath }
}
