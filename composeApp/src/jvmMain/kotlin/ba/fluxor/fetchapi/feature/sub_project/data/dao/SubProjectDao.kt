package ba.fluxor.fetchapi.feature.sub_project.data.dao

import ba.fluxor.fetchapi.feature.sub_project.data.SubProject
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

class SubProjectDao(private val connection: Connection) {

  fun insert(projectId: Long, name: String, authType: String, authConfig: String?): Long {
    connection.prepareStatement(
      "INSERT INTO sub_project(project_id, name, auth_type, auth_config) VALUES(?,?,?,?)",
      Statement.RETURN_GENERATED_KEYS,
    ).use { stmt ->
      stmt.setLong(1, projectId)
      stmt.setString(2, name)
      stmt.setString(3, authType)
      if (authConfig != null) stmt.setString(4, authConfig) else stmt.setNull(4, java.sql.Types.VARCHAR)
      stmt.executeUpdate()
      stmt.generatedKeys.use { keys ->
        if (keys.next()) return keys.getLong(1)
        error("Failed to retrieve generated id for sub_project")
      }
    }
  }

  fun update(id: Long, name: String, authType: String, authConfig: String?): Int {
    connection.prepareStatement("UPDATE sub_project SET name=?, auth_type=?, auth_config=? WHERE id=?").use { stmt ->
      stmt.setString(1, name)
      stmt.setString(2, authType)
      if (authConfig != null) stmt.setString(3, authConfig) else stmt.setNull(3, java.sql.Types.VARCHAR)
      stmt.setLong(4, id)
      return stmt.executeUpdate()
    }
  }

  fun delete(id: Long): Int {
    connection.prepareStatement("DELETE FROM sub_project WHERE id=?").use { stmt ->
      stmt.setLong(1, id)
      return stmt.executeUpdate()
    }
  }

  fun findAllByProjectId(projectId: Long): List<SubProject> {
    connection.prepareStatement(
      "SELECT id, project_id, name, auth_type, auth_config FROM sub_project WHERE project_id=? ORDER BY id"
    ).use { stmt ->
      stmt.setLong(1, projectId)
      stmt.executeQuery().use { rs ->
        val result = mutableListOf<SubProject>()
        while (rs.next()) result += rs.toSubProject()
        return result
      }
    }
  }

  fun findById(id: Long): SubProject? {
    connection.prepareStatement(
      "SELECT id, project_id, name, auth_type, auth_config FROM sub_project WHERE id=?"
    ).use { stmt ->
      stmt.setLong(1, id)
      stmt.executeQuery().use { rs ->
        return if (rs.next()) rs.toSubProject() else null
      }
    }
  }

  private fun ResultSet.toSubProject(): SubProject =
    SubProject(
      id = getLong("id"),
      projectId = getLong("project_id"),
      name = getString("name"),
      authType = getString("auth_type"),
      authConfig = getString("auth_config"),
    )
}
