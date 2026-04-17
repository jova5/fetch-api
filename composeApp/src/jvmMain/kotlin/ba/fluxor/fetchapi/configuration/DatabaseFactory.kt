package ba.fluxor.fetchapi.configuration

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import ba.fluxor.fetchapi.db.FetchApiDatabase
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.Properties

object DatabaseFactory {

  private var _connection: Connection? = null

  val connection: Connection
    get() = _connection ?: error("DatabaseFactory not initialized — call init() first")

  fun init() {
    if (_connection != null) return

    val dbPath = resolveDbPath()
    val url = "jdbc:sqlite:$dbPath"

    // Phase 1: SQLDelight driver for schema management
    val driver: SqlDriver = JdbcSqliteDriver(url, Properties())
    try {
      val currentVersion = getVersion(driver)
      val tablesExist = tablesExist(driver)

      when {
        currentVersion == 0L && !tablesExist -> {
          FetchApiDatabase.Schema.create(driver)
          setVersion(driver, 1)
        }
        currentVersion == 0L && tablesExist -> {
          setVersion(driver, 1)
        }
      }
    } finally {
      driver.close()
    }

    // Phase 2: Plain JDBC connection for DAOs
    val conn = DriverManager.getConnection(url)
    conn.createStatement().use { stmt ->
      stmt.execute("PRAGMA encoding = 'UTF-8';")
      stmt.execute("PRAGMA foreign_keys = ON;")
    }
    _connection = conn

    println("Database initialized")
  }

  private fun getVersion(driver: SqlDriver): Long {
    return driver.executeQuery(
      identifier = null,
      sql = "PRAGMA user_version;",
      mapper = { cursor ->
        cursor.next()
        QueryResult.Value(cursor.getLong(0) ?: 0L)
      },
      parameters = 0
    ).value
  }

  private fun setVersion(driver: SqlDriver, version: Int) {
    driver.execute(null, "PRAGMA user_version = $version;", 0)
  }

  private fun tablesExist(driver: SqlDriver): Boolean {
    return driver.executeQuery(
      identifier = null,
      sql = "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='project';",
      mapper = { cursor ->
        cursor.next()
        QueryResult.Value((cursor.getLong(0) ?: 0L) > 0)
      },
      parameters = 0
    ).value
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
