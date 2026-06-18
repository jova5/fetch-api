package ba.fluxor.fetchapi.feature.folder.data.dao

import ba.fluxor.fetchapi.feature.folder.data.Folder
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Types

class FolderDao(private val connection: Connection) {

  fun insert(subProjectId: Long, name: String, parentFolderId: Long?, position: Int): Long {
    connection.prepareStatement(
      "INSERT INTO folder(sub_project_id, name, parent_folder_id, position) VALUES(?,?,?,?)",
      Statement.RETURN_GENERATED_KEYS,
    ).use { stmt ->
      stmt.setLong(1, subProjectId)
      stmt.setString(2, name)
      if (parentFolderId != null) stmt.setLong(3, parentFolderId) else stmt.setNull(3, Types.INTEGER)
      stmt.setInt(4, position)
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

  fun updatePosition(id: Long, position: Int): Int {
    connection.prepareStatement("UPDATE folder SET position=? WHERE id=?").use { stmt ->
      stmt.setInt(1, position)
      stmt.setLong(2, id)
      return stmt.executeUpdate()
    }
  }

  /** Highest sibling position within a parent group; -1 when the group is empty. */
  fun maxPosition(subProjectId: Long, parentFolderId: Long?): Int {
    val sql = if (parentFolderId != null) {
      "SELECT COALESCE(MAX(position), -1) FROM folder WHERE sub_project_id=? AND parent_folder_id=?"
    } else {
      "SELECT COALESCE(MAX(position), -1) FROM folder WHERE sub_project_id=? AND parent_folder_id IS NULL"
    }
    connection.prepareStatement(sql).use { stmt ->
      stmt.setLong(1, subProjectId)
      if (parentFolderId != null) stmt.setLong(2, parentFolderId)
      stmt.executeQuery().use { rs ->
        return if (rs.next()) rs.getInt(1) else -1
      }
    }
  }

  /**
   * Re-parents the folder (new sub-project and/or parent folder) and sets its position. Because
   * every folder and request carries its own [sub_project_id], the whole subtree is re-stamped with
   * [subProjectId] via a recursive CTE over [parent_folder_id] so descendants follow the move.
   */
  fun move(id: Long, subProjectId: Long, parentFolderId: Long?, position: Int): Int {
    val moved = connection.prepareStatement(
      "UPDATE folder SET sub_project_id=?, parent_folder_id=?, position=? WHERE id=?"
    ).use { stmt ->
      stmt.setLong(1, subProjectId)
      if (parentFolderId != null) stmt.setLong(2, parentFolderId) else stmt.setNull(2, Types.INTEGER)
      stmt.setInt(3, position)
      stmt.setLong(4, id)
      stmt.executeUpdate()
    }

    val descendantsCte = """
      WITH RECURSIVE descendants(id) AS (
        SELECT id FROM folder WHERE id = ?
        UNION ALL
        SELECT f.id FROM folder f JOIN descendants d ON f.parent_folder_id = d.id
      )
    """.trimIndent()

    connection.prepareStatement(
      "$descendantsCte UPDATE folder SET sub_project_id=? WHERE id IN (SELECT id FROM descendants)"
    ).use { stmt ->
      stmt.setLong(1, id)
      stmt.setLong(2, subProjectId)
      stmt.executeUpdate()
    }

    connection.prepareStatement(
      "$descendantsCte UPDATE request SET sub_project_id=? WHERE folder_id IN (SELECT id FROM descendants)"
    ).use { stmt ->
      stmt.setLong(1, id)
      stmt.setLong(2, subProjectId)
      stmt.executeUpdate()
    }

    return moved
  }

  /**
   * Deletes the folder together with all of its descendant folders and every request contained
   * in any of them. The descendant set is gathered with a recursive CTE over [parent_folder_id].
   * Returns the number of folder rows removed.
   */
  fun delete(id: Long): Int {
    val descendantsCte = """
      WITH RECURSIVE descendants(id) AS (
        SELECT id FROM folder WHERE id = ?
        UNION ALL
        SELECT f.id FROM folder f JOIN descendants d ON f.parent_folder_id = d.id
      )
    """.trimIndent()

    connection.prepareStatement(
      "$descendantsCte DELETE FROM request WHERE folder_id IN (SELECT id FROM descendants)"
    ).use { stmt ->
      stmt.setLong(1, id)
      stmt.executeUpdate()
    }

    connection.prepareStatement(
      "$descendantsCte DELETE FROM folder WHERE id IN (SELECT id FROM descendants)"
    ).use { stmt ->
      stmt.setLong(1, id)
      return stmt.executeUpdate()
    }
  }

  fun findAllBySubProjectId(subProjectId: Long): List<Folder> {
    connection.prepareStatement(
      "SELECT id, sub_project_id, name, parent_folder_id FROM folder WHERE sub_project_id=? ORDER BY position, id"
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
    connection.prepareStatement(
      "SELECT id, sub_project_id, name, parent_folder_id FROM folder WHERE id=?"
    ).use { stmt ->
      stmt.setLong(1, id)
      stmt.executeQuery().use { rs ->
        return if (rs.next()) rs.toFolder() else null
      }
    }
  }

  private fun ResultSet.toFolder(): Folder {
    val parentFolderId = getLong("parent_folder_id")
    val parentFolderIdOrNull = if (wasNull()) null else parentFolderId
    return Folder(
      id = getLong("id"),
      subProjectId = getLong("sub_project_id"),
      name = getString("name"),
      parentFolderId = parentFolderIdOrNull,
    )
  }
}
