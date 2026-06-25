package ba.fluxor.fetchapi.ui.util

import com.sun.jna.Platform
import java.awt.FileDialog
import java.awt.Frame
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.nfd.NativeFileDialog

/**
 * Opens the OS file-open dialog and returns the absolute paths of the chosen files.
 * Returns an empty list when the user cancels. Blocking (modal) — call from a click handler.
 *
 * On Windows it uses LWJGL's nativefiledialog-extended (the modern Common Item Dialog, which
 * follows the system dark theme once the process opts in — see [WindowsDarkMode]). Other
 * platforms fall back to the native AWT dialog.
 */
fun pickFiles(title: String = "Choose files", multiple: Boolean = true): List<String> =
  if (Platform.isWindows()) pickFilesNfd(multiple) else pickFilesAwt(title, multiple)

private fun pickFilesAwt(title: String, multiple: Boolean): List<String> {
  val dialog = FileDialog(null as Frame?, title, FileDialog.LOAD).apply {
    isMultipleMode = multiple
    isVisible = true
  }
  return dialog.files.map { it.absolutePath }
}

private var nfdInitialized = false

private fun pickFilesNfd(multiple: Boolean): List<String> {
  if (!nfdInitialized) {
    NativeFileDialog.NFD_Init()
    nfdInitialized = true
  }
  return if (multiple) pickMultipleNfd() else pickSingleNfd()
}

private fun pickSingleNfd(): List<String> {
  MemoryStack.stackPush().use { stack ->
    val outPath = stack.mallocPointer(1)
    if (NativeFileDialog.NFD_OpenDialog(outPath, null, null as CharSequence?) != NativeFileDialog.NFD_OKAY) {
      return emptyList()
    }

    val ptr = outPath.get(0)
    val path = MemoryUtil.memUTF8(ptr)
    NativeFileDialog.NFD_FreePath(ptr)
    return listOf(path)
  }
}

private fun pickMultipleNfd(): List<String> {
  MemoryStack.stackPush().use { stack ->
    val outPaths = stack.mallocPointer(1)
    if (NativeFileDialog.NFD_OpenDialogMultiple(outPaths, null, null as CharSequence?) != NativeFileDialog.NFD_OKAY) {
      return emptyList()
    }

    val pathSet = outPaths.get(0)
    return try {
      val count = stack.mallocInt(1)
      NativeFileDialog.NFD_PathSet_GetCount(pathSet, count)
      val total = count.get(0)

      val paths = ArrayList<String>(total)
      val outPath = stack.mallocPointer(1)
      for (i in 0 until total) {
        if (NativeFileDialog.NFD_PathSet_GetPath(pathSet, i, outPath) == NativeFileDialog.NFD_OKAY) {
          val ptr = outPath.get(0)
          paths.add(MemoryUtil.memUTF8(ptr))
          NativeFileDialog.NFD_PathSet_FreePath(ptr)
        }
      }
      paths
    } finally {
      NativeFileDialog.NFD_PathSet_Free(pathSet)
    }
  }
}
