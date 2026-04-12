package ba.fluxor.fetchapi.feature.request.data.dao

import ba.fluxor.fetchapi.feature.request.data.Request
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Types

class RequestDao(private val connection: Connection) {

  fun insert(
    subProjectId: Long,
    folderId: Long?,
    name: String,
    method: String,
    url: String,
    headers: String?,
    body: String?,
  ): Long {
    connection.prepareStatement(
      "INSERT INTO request(sub_project_id, folder_id, name, method, url, headers, body) VALUES(?,?,?,?,?,?,?)",
      Statement.RETURN_GENERATED_KEYS,
    ).use { stmt ->
      stmt.setLong(1, subProjectId)
      if (folderId != null) stmt.setLong(2, folderId) else stmt.setNull(2, Types.INTEGER)
      stmt.setString(3, name)
      stmt.setString(4, method)
      stmt.setString(5, url)
      if (headers != null) stmt.setString(6, headers) else stmt.setNull(6, Types.VARCHAR)
      if (body != null) stmt.setString(7, body) else stmt.setNull(7, Types.VARCHAR)
      stmt.executeUpdate()
      stmt.generatedKeys.use { keys ->
        if (keys.next()) return keys.getLong(1)
        error("Failed to retrieve generated id for request")
      }
    }
  }

  fun update(
    id: Long,
    folderId: Long?,
    name: String,
    method: String,
    url: String,
    headers: String?,
    body: String?,
  ): Int {
    connection.prepareStatement(
      "UPDATE request SET folder_id=?, name=?, method=?, url=?, headers=?, body=? WHERE id=?"
    ).use { stmt ->
      if (folderId != null) stmt.setLong(1, folderId) else stmt.setNull(1, Types.INTEGER)
      stmt.setString(2, name)
      stmt.setString(3, method)
      stmt.setString(4, url)
      if (headers != null) stmt.setString(5, headers) else stmt.setNull(5, Types.VARCHAR)
      if (body != null) stmt.setString(6, body) else stmt.setNull(6, Types.VARCHAR)
      stmt.setLong(7, id)
      return stmt.executeUpdate()
    }
  }

  fun delete(id: Long): Int {
    connection.prepareStatement("DELETE FROM request WHERE id=?").use { stmt ->
      stmt.setLong(1, id)
      return stmt.executeUpdate()
    }
  }

  fun findAllBySubProjectId(subProjectId: Long): List<Request> {
    connection.prepareStatement(
      "SELECT id, sub_project_id, folder_id, name, method, url, headers, body FROM request WHERE sub_project_id=? ORDER BY id"
    ).use { stmt ->
      stmt.setLong(1, subProjectId)
      stmt.executeQuery().use { rs ->
        val result = mutableListOf<Request>()
        while (rs.next()) result += rs.toRequest()
        return result
      }
    }
  }

  fun findAllByFolderId(folderId: Long): List<Request> {
    connection.prepareStatement(
      "SELECT id, sub_project_id, folder_id, name, method, url, headers, body FROM request WHERE folder_id=? ORDER BY id"
    ).use { stmt ->
      stmt.setLong(1, folderId)
      stmt.executeQuery().use { rs ->
        val result = mutableListOf<Request>()
        while (rs.next()) result += rs.toRequest()
        return result
      }
    }
  }

  fun findAllLooseBySubProjectId(subProjectId: Long): List<Request> {
    connection.prepareStatement(
      "SELECT id, sub_project_id, folder_id, name, method, url, headers, body FROM request WHERE sub_project_id=? AND folder_id IS NULL ORDER BY id"
    ).use { stmt ->
      stmt.setLong(1, subProjectId)
      stmt.executeQuery().use { rs ->
        val result = mutableListOf<Request>()
        while (rs.next()) result += rs.toRequest()
        return result
      }
    }
  }

  fun findById(id: Long): Request? {
    connection.prepareStatement(
      "SELECT id, sub_project_id, folder_id, name, method, url, headers, body FROM request WHERE id=?"
    ).use { stmt ->
      stmt.setLong(1, id)
      stmt.executeQuery().use { rs ->
        return if (rs.next()) rs.toRequest() else null
      }
    }
  }

  private fun ResultSet.toRequest(): Request {
    val folderId = getLong("folder_id")
    return Request(
      id = getLong("id"),
      subProjectId = getLong("sub_project_id"),
      folderId = if (wasNull()) null else folderId,
      name = getString("name"),
      method = getString("method"),
      url = getString("url"),
      headers = getString("headers"),
      body = getString("body"),
    )
  }
}
