package ba.fluxor.fetchapi.feature.sub_project.data.dao

import ba.fluxor.fetchapi.configuration.transaction
import ba.fluxor.fetchapi.feature.sub_project.data.SubProjectVariable
import java.sql.Connection
import java.sql.ResultSet

class SubProjectVariableDao(private val connection: Connection) {

  fun findAllBySubProjectId(subProjectId: Long): List<SubProjectVariable> {
    connection.prepareStatement(
      "SELECT id, sub_project_id, key, value FROM sub_project_variable WHERE sub_project_id=? ORDER BY id"
    ).use { stmt ->
      stmt.setLong(1, subProjectId)
      stmt.executeQuery().use { rs ->
        val result = mutableListOf<SubProjectVariable>()
        while (rs.next()) result += rs.toVariable()
        return result
      }
    }
  }

  fun replaceAllForSubProject(subProjectId: Long, items: List<SubProjectVariable>) = connection.transaction {
    connection.prepareStatement("DELETE FROM sub_project_variable WHERE sub_project_id=?").use { stmt ->
      stmt.setLong(1, subProjectId)
      stmt.executeUpdate()
    }
    if (items.isNotEmpty()) {
      connection.prepareStatement(
        "INSERT INTO sub_project_variable(sub_project_id, key, value) VALUES(?,?,?)"
      ).use { stmt ->
        for (item in items) {
          stmt.setLong(1, subProjectId)
          stmt.setString(2, item.key)
          if (item.value != null) stmt.setString(3, item.value) else stmt.setNull(3, java.sql.Types.VARCHAR)
          stmt.addBatch()
        }
        stmt.executeBatch()
      }
    }
  }

  private fun ResultSet.toVariable(): SubProjectVariable =
    SubProjectVariable(
      id = getLong("id"),
      subProjectId = getLong("sub_project_id"),
      key = getString("key"),
      value = getString("value"),
    )
}
