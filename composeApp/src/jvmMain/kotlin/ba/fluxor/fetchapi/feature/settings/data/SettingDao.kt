package ba.fluxor.fetchapi.feature.settings.data

import java.sql.Connection

class SettingDao(private val connection: Connection) {

  fun get(key: String): String? {
    connection.prepareStatement("SELECT value FROM setting WHERE key=?").use { stmt ->
      stmt.setString(1, key)
      stmt.executeQuery().use { rs ->
        return if (rs.next()) rs.getString("value") else null
      }
    }
  }

  fun upsert(key: String, value: String) {
    connection.prepareStatement(
      "INSERT INTO setting(key, value) VALUES(?, ?) " +
        "ON CONFLICT(key) DO UPDATE SET value=excluded.value",
    ).use { stmt ->
      stmt.setString(1, key)
      stmt.setString(2, value)
      stmt.executeUpdate()
    }
  }
}
