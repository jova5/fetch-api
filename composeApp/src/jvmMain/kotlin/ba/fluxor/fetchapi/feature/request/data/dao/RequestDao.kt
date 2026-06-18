package ba.fluxor.fetchapi.feature.request.data.dao

import ba.fluxor.fetchapi.feature.request.data.BodyConfig
import ba.fluxor.fetchapi.feature.request.data.KeyValueEntry
import ba.fluxor.fetchapi.feature.request.data.RawLanguage
import ba.fluxor.fetchapi.feature.request.data.Request
import ba.fluxor.fetchapi.feature.request.data.RequestCodecs
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Types

class RequestDao(private val connection: Connection) {

  private val columns =
    "id, sub_project_id, folder_id, name, method, url, headers, body, params, headers_json, body_config, auth_type, auth_config, excluded_auto_headers"

  fun insert(request: Request, position: Int): Long {
    connection.prepareStatement(
      "INSERT INTO request(sub_project_id, folder_id, name, method, url, params, headers_json, body_config, auth_type, auth_config, excluded_auto_headers, position) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
      Statement.RETURN_GENERATED_KEYS,
    ).use { stmt ->
      bindWriteParams(stmt, request, startIndex = 1)
      stmt.setInt(12, position)
      stmt.executeUpdate()
      stmt.generatedKeys.use { keys ->
        if (keys.next()) return keys.getLong(1)
        error("Failed to retrieve generated id for request")
      }
    }
  }

  fun update(request: Request, id: Long): Int {
    connection.prepareStatement(
      "UPDATE request SET folder_id=?, name=?, method=?, url=?, params=?, headers_json=?, body_config=?, auth_type=?, auth_config=?, excluded_auto_headers=?, headers=NULL, body=NULL WHERE id=?"
    ).use { stmt ->
      if (request.folderId != null) stmt.setLong(1, request.folderId) else stmt.setNull(1, Types.INTEGER)
      stmt.setString(2, request.name)
      stmt.setString(3, request.method)
      stmt.setString(4, request.url)
      setNullableString(stmt, 5, RequestCodecs.encodeKeyValues(request.params))
      setNullableString(stmt, 6, RequestCodecs.encodeKeyValues(request.headers))
      setNullableString(stmt, 7, RequestCodecs.encodeBody(request.body))
      stmt.setString(8, request.authType)
      setNullableString(stmt, 9, request.authConfig)
      setNullableString(stmt, 10, RequestCodecs.encodeStringSet(request.excludedAutoHeaders))
      stmt.setLong(11, id)
      return stmt.executeUpdate()
    }
  }

  fun delete(id: Long): Int {
    connection.prepareStatement("DELETE FROM request WHERE id=?").use { stmt ->
      stmt.setLong(1, id)
      return stmt.executeUpdate()
    }
  }

  fun updatePosition(id: Long, position: Int): Int {
    connection.prepareStatement("UPDATE request SET position=? WHERE id=?").use { stmt ->
      stmt.setInt(1, position)
      stmt.setLong(2, id)
      return stmt.executeUpdate()
    }
  }

  /** Re-parents the request (new sub-project and/or folder) and sets its position. */
  fun move(id: Long, subProjectId: Long, folderId: Long?, position: Int): Int {
    connection.prepareStatement(
      "UPDATE request SET sub_project_id=?, folder_id=?, position=? WHERE id=?"
    ).use { stmt ->
      stmt.setLong(1, subProjectId)
      if (folderId != null) stmt.setLong(2, folderId) else stmt.setNull(2, Types.INTEGER)
      stmt.setInt(3, position)
      stmt.setLong(4, id)
      return stmt.executeUpdate()
    }
  }

  /** Highest sibling position within a container (a folder, or the loose group of a sub-project). */
  fun maxPosition(subProjectId: Long, folderId: Long?): Int {
    val sql = if (folderId != null) {
      "SELECT COALESCE(MAX(position), -1) FROM request WHERE folder_id=?"
    } else {
      "SELECT COALESCE(MAX(position), -1) FROM request WHERE sub_project_id=? AND folder_id IS NULL"
    }
    connection.prepareStatement(sql).use { stmt ->
      if (folderId != null) stmt.setLong(1, folderId) else stmt.setLong(1, subProjectId)
      stmt.executeQuery().use { rs ->
        return if (rs.next()) rs.getInt(1) else -1
      }
    }
  }

  fun findAllBySubProjectId(subProjectId: Long): List<Request> {
    connection.prepareStatement(
      "SELECT $columns FROM request WHERE sub_project_id=? ORDER BY position, id"
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
      "SELECT $columns FROM request WHERE folder_id=? ORDER BY position, id"
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
      "SELECT $columns FROM request WHERE sub_project_id=? AND folder_id IS NULL ORDER BY position, id"
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
      "SELECT $columns FROM request WHERE id=?"
    ).use { stmt ->
      stmt.setLong(1, id)
      stmt.executeQuery().use { rs ->
        return if (rs.next()) rs.toRequest() else null
      }
    }
  }

  private fun bindWriteParams(stmt: java.sql.PreparedStatement, request: Request, startIndex: Int) {
    var i = startIndex
    stmt.setLong(i++, request.subProjectId)
    if (request.folderId != null) stmt.setLong(i++, request.folderId) else stmt.setNull(i++, Types.INTEGER)
    stmt.setString(i++, request.name)
    stmt.setString(i++, request.method)
    stmt.setString(i++, request.url)
    setNullableString(stmt, i++, RequestCodecs.encodeKeyValues(request.params))
    setNullableString(stmt, i++, RequestCodecs.encodeKeyValues(request.headers))
    setNullableString(stmt, i++, RequestCodecs.encodeBody(request.body))
    stmt.setString(i++, request.authType)
    setNullableString(stmt, i++, request.authConfig)
    setNullableString(stmt, i, RequestCodecs.encodeStringSet(request.excludedAutoHeaders))
  }

  private fun setNullableString(stmt: java.sql.PreparedStatement, index: Int, value: String?) {
    if (value != null) stmt.setString(index, value) else stmt.setNull(index, Types.VARCHAR)
  }

  private fun ResultSet.toRequest(): Request {
    val folderId = getLong("folder_id")
    val folderIdOrNull = if (wasNull()) null else folderId

    val headersJson = getString("headers_json")
    val legacyHeaders = getString("headers")
    val headers = when {
      !headersJson.isNullOrBlank() -> RequestCodecs.decodeKeyValues(headersJson)
      !legacyHeaders.isNullOrBlank() -> listOf(KeyValueEntry(value = legacyHeaders, description = "legacy"))
      else -> emptyList()
    }

    val bodyConfig = getString("body_config")
    val legacyBody = getString("body")
    val body = when {
      !bodyConfig.isNullOrBlank() -> RequestCodecs.decodeBody(bodyConfig)
      !legacyBody.isNullOrBlank() -> BodyConfig.Raw(language = RawLanguage.TEXT, content = legacyBody)
      else -> BodyConfig.None
    }

    val authType = getString("auth_type") ?: "INHERIT"
    val authConfig = getString("auth_config")

    return Request(
      id = getLong("id"),
      subProjectId = getLong("sub_project_id"),
      folderId = folderIdOrNull,
      name = getString("name"),
      method = getString("method"),
      url = getString("url"),
      params = RequestCodecs.decodeKeyValues(getString("params")),
      headers = headers,
      body = body,
      authType = authType,
      authConfig = authConfig,
      excludedAutoHeaders = RequestCodecs.decodeStringSet(getString("excluded_auto_headers")),
    )
  }
}
