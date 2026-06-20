package ba.fluxor.fetchapi.feature.request.data

import ba.fluxor.fetchapi.feature.request.data.dao.RequestDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RequestRepository(private val dao: RequestDao) {

  suspend fun getAllByFolderId(folderId: Long): List<Request> = withContext(Dispatchers.IO) {
    dao.findAllByFolderId(folderId)
  }

  suspend fun getAllLooseBySubProjectId(subProjectId: Long): List<Request> = withContext(Dispatchers.IO) {
    dao.findAllLooseBySubProjectId(subProjectId)
  }

  suspend fun getById(id: Long): Request? = withContext(Dispatchers.IO) {
    dao.findById(id)
  }

  suspend fun create(request: Request): Request = withContext(Dispatchers.IO) {
    val position = dao.maxPosition(request.subProjectId, request.folderId) + 1
    val id = dao.insert(request, position)
    dao.findById(id) ?: error("Inserted request with id=$id not found")
  }

  /** Re-parents the request (new sub-project and/or folder) and sets its position. */
  suspend fun move(id: Long, subProjectId: Long, folderId: Long?, position: Int): Unit =
    withContext(Dispatchers.IO) {
      dao.move(id, subProjectId, folderId, position)
    }

  /** Bulk re-stamps [subProjectId] onto every request contained directly in any of [folderIds]. */
  suspend fun restampSubProject(folderIds: List<Long>, subProjectId: Long): Unit =
    withContext(Dispatchers.IO) {
      dao.restampSubProject(folderIds, subProjectId)
    }

  /** Persists the given sibling order by writing each id's index back as its position. */
  suspend fun updatePositions(orderedIds: List<Long>) = withContext(Dispatchers.IO) {
    orderedIds.forEachIndexed { index, id -> dao.updatePosition(id, index) }
  }

  suspend fun update(request: Request, id: Long): Request = withContext(Dispatchers.IO) {
    dao.update(request, id)
    dao.findById(id) ?: error("Updated request with id=$id not found")
  }

  suspend fun delete(id: Long): Boolean = withContext(Dispatchers.IO) {
    dao.delete(id) > 0
  }
}
