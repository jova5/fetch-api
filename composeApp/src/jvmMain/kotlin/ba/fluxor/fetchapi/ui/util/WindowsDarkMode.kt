package ba.fluxor.fetchapi.ui.util

import com.sun.jna.Function
import com.sun.jna.NativeLibrary
import com.sun.jna.Platform
import com.sun.jna.platform.win32.Kernel32

/**
 * Opts the whole process into Windows dark mode so that native dialogs (e.g. the file-open
 * dialog) follow the system theme. Without this, Win32 common dialogs stay light even when
 * Windows is in dark mode.
 *
 * Uses the undocumented `uxtheme.dll` export `SetPreferredAppMode`, which is only exposed by
 * ordinal (135) and has been stable since Windows 10 1809. Wrapped in [runCatching] so the app
 * degrades gracefully (dialog stays light) if the ordinal ever disappears.
 */
object WindowsDarkMode {

  // PreferredAppMode: Default = 0, AllowDark = 1, ForceDark = 2, ForceLight = 3.
  // AllowDark makes native dialogs follow the system theme.
  private const val ALLOW_DARK = 1
  private const val SET_PREFERRED_APP_MODE_ORDINAL = 135

  private var initialized = false

  fun enable() {
    if (initialized || !Platform.isWindows()) return
    initialized = true

    runCatching {
      NativeLibrary.getInstance("uxtheme") // ensure uxtheme.dll is loaded
      val hModule = Kernel32.INSTANCE.GetModuleHandle("uxtheme")
      val proc = Kernel32.INSTANCE.GetProcAddress(hModule, SET_PREFERRED_APP_MODE_ORDINAL)
      Function.getFunction(proc).invokeInt(arrayOf<Any>(ALLOW_DARK))
    }
  }
}
