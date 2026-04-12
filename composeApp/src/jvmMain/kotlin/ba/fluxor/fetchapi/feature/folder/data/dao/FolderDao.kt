package ba.fluxor.fetchapi.feature.folder.data.dao

import ba.fluxor.fetchapi.feature.folder.data.Folder
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

class FolderDao(private val connection: Connection) {

  fun insert(subProjectId: Long, name: String): Long {
    connection.prepareStatement(
      "INSERT INTO folder(sub_project_id, name) VALUES(?,?)",
      Statement.RETURN_GENERATED_KEYS,
    ).use { stmt ->
      stmt.setLong(1, subProjectId)
      stmt.setString(2, name)
      stmt.executeUpdate()
      stmt.generatedKeys.use { keys ->
        if (keys.next()) return keys.getLong(1)
        error("Failed to retrieve generated id for folder")
      }
    }
  }

  fun update(id: Long, name: String): Int {
    connection.prepareStatement("UPDATE folder SET name=? WHERE id=?").use { stmt ->
      stmt.setString(1, name)
      stmt.setLong(2, id)
      return stmt.executeUpdate()
    }
  }

  fun delete(id: Long): Int {
    connection.prepareStatement("DELETE FROM folder WHERE id=?").use { stmt ->
      stmt.setLong(1, id)
      return stmt.executeUpdate()
    }
  }

  fun findAllBySubProjectId(subProjectId: Long): List<Folder> {
    connection.prepareStatement(
      "SELECT id, sub_project_id, name FROM folder WHERE sub_project_id=? ORDER BY id"
    ).use { stmt ->
      stmt.setLong(1, subProjectId)
      stmt.executeQuery().use { rs ->
        val result = mutableListOf<Folder>()
        while (rs.next()) result += rs.toFolder()
        return result
      }
    }
  }

  fun findById(id: Long): Folder? {
    connection.prepareStatement("SELECT id, sub_project_id, name FROM folder WHERE id=?").use { stmt ->
      stmt.setLong(1, id)
      stmt.executeQuery().use { rs ->
        return if (rs.next()) rs.toFolder() else null
      }
    }
  }

  fun existsByNameAndSubProjectId(name: String, subProjectId: Long): Boolean {
    connection.prepareStatement("SELECT 1 FROM folder WHERE name=? AND sub_project_id=?").use { stmt ->
      stmt.setString(1, name)
      stmt.setLong(2, subProjectId)
      stmt.executeQuery().use { rs ->
        return rs.next()
      }
    }
  }

  fun existsByNameAndSubProjectIdAndIdNot(name: String, subProjectId: Long, id: Long): Boolean {
    connection.prepareStatement("SELECT 1 FROM folder WHERE name=? AND sub_project_id=? AND id!=?").use { stmt ->
      stmt.setString(1, name)
      stmt.setLong(2, subProjectId)
      stmt.setLong(3, id)
      stmt.executeQuery().use { rs ->
        return rs.next()
      }
    }
  }

  private fun ResultSet.toFolder(): Folder =
    Folder(
      id = getLong("id"),
      subProjectId = getLong("sub_project_id"),
      name = getString("name"),
    )
}
