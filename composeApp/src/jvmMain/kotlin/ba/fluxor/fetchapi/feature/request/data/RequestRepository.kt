package ba.fluxor.fetchapi.feature.request.data

import ba.fluxor.fetchapi.feature.request.data.dao.RequestDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RequestRepository(private val dao: RequestDao) {

  suspend fun getAllBySubProjectId(subProjectId: Long): List<Request> = withContext(Dispatchers.IO) {
    dao.findAllBySubProjectId(subProjectId)
  }

  suspend fun getAllByFolderId(folderId: Long): List<Request> = withContext(Dispatchers.IO) {
    dao.findAllByFolderId(folderId)
  }

  suspend fun getAllLooseBySubProjectId(subProjectId: Long): List<Request> = withContext(Dispatchers.IO) {
    dao.findAllLooseBySubProjectId(subProjectId)
  }

  suspend fun getById(id: Long): Request? = withContext(Dispatchers.IO) {
    dao.findById(id)
  }

  suspend fun create(
    subProjectId: Long,
    folderId: Long?,
    name: String,
    method: String,
    url: String,
    headers: String? = null,
    body: String? = null,
  ): Request = withContext(Dispatchers.IO) {
    val id = dao.insert(subProjectId, folderId, name, method, url, headers, body)
    dao.findById(id) ?: error("Inserted request with id=$id not found")
  }

  suspend fun update(
    id: Long,
    folderId: Long?,
    name: String,
    method: String,
    url: String,
    headers: String?,
    body: String?,
  ): Request = withContext(Dispatchers.IO) {
    dao.update(id, folderId, name, method, url, headers, body)
    dao.findById(id) ?: error("Updated request with id=$id not found")
  }

  suspend fun delete(id: Long): Boolean = withContext(Dispatchers.IO) {
    dao.delete(id) > 0
  }
}
