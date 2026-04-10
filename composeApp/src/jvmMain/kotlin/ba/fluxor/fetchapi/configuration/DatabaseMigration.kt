package ba.fluxor.fetchapi.configuration

import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import java.sql.DriverManager

object DatabaseMigrator {

  fun migrate() {
    val dbPath = getDbPath()
    val url = "jdbc:sqlite:$dbPath"

    DriverManager.getConnection(url).use { connection ->
      connection.createStatement().execute("PRAGMA encoding = 'UTF-8';")

      val database = DatabaseFactory.getInstance()
        .findCorrectDatabaseImplementation(JdbcConnection(connection))

      Liquibase(
        "db/changelog/db.changelog-master.xml",
        ClassLoaderResourceAccessor(),
        database
      ).use {
        it.update("") // empty string = run all contexts
      }
    }

    println("Database migration complete")
  }

  private fun getDbPath(): String {

    val appName = "FetchAPI"
    val os = System.getProperty("os.name").lowercase()

    val dir = when {
      os.contains("win") ->
        // C:\Users\<user>\AppData\Roaming\MyApp
        System.getenv("APPDATA") + "\\$appName"

      os.contains("mac") ->
        // /Users/<user>/Library/Application Support/MyApp
        System.getProperty("user.home") + "/Library/Application Support/$appName"

      else ->
        // /home/<user>/.local/share/MyApp  (Linux/Unix)
        System.getProperty("user.home") + "/.local/share/$appName"
    }

    // make sure the directory exists before SQLite tries to create the file
    java.io.File(dir).mkdirs()

    return "$dir/app.db"
  }
}
