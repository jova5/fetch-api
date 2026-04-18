package ba.fluxor.fetchapi.feature.tabs.data.dao

import ba.fluxor.fetchapi.feature.tabs.data.Tab
import ba.fluxor.fetchapi.feature.tabs.data.TabType
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

class TabDao(private val connection: Connection) {

  fun insert(projectId: Long, type: TabType, entityId: Long, position: Int): Long {
    connection.prepareStatement(
      "INSERT INTO tab(project_id, type, entity_id, position) VALUES(?,?,?,?)",
      Statement.RETURN_GENERATED_KEYS,
    ).use { stmt ->
      stmt.setLong(1, projectId)
      stmt.setString(2, type.name)
      stmt.setLong(3, entityId)
      stmt.setInt(4, position)
      stmt.executeUpdate()
      stmt.generatedKeys.use { keys ->
        if (keys.next()) return keys.getLong(1)
        error("Failed to retrieve generated id for tab")
      }
    }
  }

  fun delete(id: Long): Int {
    connection.prepareStatement("DELETE FROM tab WHERE id=?").use { stmt ->
      stmt.setLong(1, id)
      return stmt.executeUpdate()
    }
  }

  fun deleteByEntity(type: TabType, entityId: Long): Int {
    connection.prepareStatement("DELETE FROM tab WHERE type=? AND entity_id=?").use { stmt ->
      stmt.setString(1, type.name)
      stmt.setLong(2, entityId)
      return stmt.executeUpdate()
    }
  }

  fun findAllByProjectId(projectId: Long): List<Tab> {
    connection.prepareStatement(
      "SELECT id, project_id, type, entity_id, position FROM tab WHERE project_id=? ORDER BY position"
    ).use { stmt ->
      stmt.setLong(1, projectId)
      stmt.executeQuery().use { rs ->
        val result = mutableListOf<Tab>()
        while (rs.next()) result += rs.toTab()
        return result
      }
    }
  }

  fun findByEntity(projectId: Long, type: TabType, entityId: Long): Tab? {
    connection.prepareStatement(
      "SELECT id, project_id, type, entity_id, position FROM tab WHERE project_id=? AND type=? AND entity_id=?"
    ).use { stmt ->
      stmt.setLong(1, projectId)
      stmt.setString(2, type.name)
      stmt.setLong(3, entityId)
      stmt.executeQuery().use { rs ->
        return if (rs.next()) rs.toTab() else null
      }
    }
  }

  fun maxPosition(projectId: Long): Int {
    connection.prepareStatement("SELECT COALESCE(MAX(position), -1) FROM tab WHERE project_id=?").use { stmt ->
      stmt.setLong(1, projectId)
      stmt.executeQuery().use { rs ->
        return if (rs.next()) rs.getInt(1) else -1
      }
    }
  }

  private fun ResultSet.toTab(): Tab =
    Tab(
      id = getLong("id"),
      projectId = getLong("project_id"),
      type = TabType.valueOf(getString("type")),
      entityId = getLong("entity_id"),
      position = getInt("position"),
    )
}
