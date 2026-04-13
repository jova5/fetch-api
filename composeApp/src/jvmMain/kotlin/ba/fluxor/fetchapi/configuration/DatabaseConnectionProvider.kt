package ba.fluxor.fetchapi.configuration

import java.io.File
import java.sql.Connection
import java.sql.DriverManager

object DatabaseConnectionProvider {

  private var _connection: Connection? = null

  val connection: Connection
    get() = _connection ?: error("DatabaseConnectionProvider not initialized — call init() first")

  fun init() {
    if (_connection != null) return

    val dbPath = resolveDbPath()
    val conn = DriverManager.getConnection("jdbc:sqlite:$dbPath")
    conn.createStatement().use { stmt ->
      stmt.execute("PRAGMA encoding = 'UTF-8';")
      stmt.execute("PRAGMA foreign_keys = ON;")
    }
    _connection = conn
  }

  private fun resolveDbPath(): String {
    val appName = "FetchAPI"
    val os = System.getProperty("os.name").lowercase()

    val dir = when {
      os.contains("win") -> System.getenv("APPDATA") + "\\$appName"
      os.contains("mac") -> System.getProperty("user.home") + "/Library/Application Support/$appName"
      else -> System.getProperty("user.home") + "/.local/share/$appName"
    }

    File(dir).mkdirs()

    return "$dir/fetch-api.db"
  }

  fun close() {
    _connection?.close()
    _connection = null
  }
}
