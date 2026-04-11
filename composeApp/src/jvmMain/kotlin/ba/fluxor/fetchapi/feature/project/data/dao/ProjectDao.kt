package ba.fluxor.fetchapi.feature.project.data.dao

import ba.fluxor.fetchapi.feature.project.data.Project
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

class ProjectDao(private val connection: Connection) {

  fun insert(name: String): Long {
    connection.prepareStatement(
      "INSERT INTO project(name) VALUES(?)",
      Statement.RETURN_GENERATED_KEYS,
    ).use { stmt ->
      stmt.setString(1, name)
      stmt.executeUpdate()
      stmt.generatedKeys.use { keys ->
        if (keys.next()) return keys.getLong(1)
        error("Failed to retrieve generated id for project")
      }
    }
  }

  fun update(id: Long, name: String): Int {
    connection.prepareStatement("UPDATE project SET name=? WHERE id=?").use { stmt ->
      stmt.setString(1, name)
      stmt.setLong(2, id)
      return stmt.executeUpdate()
    }
  }

  fun delete(id: Long): Int {
    connection.prepareStatement("DELETE FROM project WHERE id=?").use { stmt ->
      stmt.setLong(1, id)
      return stmt.executeUpdate()
    }
  }

  fun findAll(): List<Project> {
    connection.prepareStatement("SELECT id, name FROM project ORDER BY id").use { stmt ->
      stmt.executeQuery().use { rs ->
        val result = mutableListOf<Project>()
        while (rs.next()) result += rs.toProject()
        return result
      }
    }
  }

  fun findById(id: Long): Project? {
    connection.prepareStatement("SELECT id, name FROM project WHERE id=?").use { stmt ->
      stmt.setLong(1, id)
      stmt.executeQuery().use { rs ->
        return if (rs.next()) rs.toProject() else null
      }
    }
  }

  private fun ResultSet.toProject(): Project =
    Project(id = getLong("id"), name = getString("name"))
}
