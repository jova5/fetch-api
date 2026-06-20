package ba.fluxor.fetchapi.feature.folder.data

import ba.fluxor.fetchapi.feature.folder.data.dao.FolderDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FolderRepository(private val dao: FolderDao) {

  suspend fun getAllBySubProjectId(subProjectId: Long): List<Folder> = withContext(Dispatchers.IO) {
    dao.findAllBySubProjectId(subProjectId)
  }

  suspend fun create(subProjectId: Long, name: String, parentFolderId: Long? = null): Folder =
    withContext(Dispatchers.IO) {
      val position = dao.maxPosition(subProjectId, parentFolderId) + 1
      val id = dao.insert(subProjectId, name, parentFolderId, position)
      dao.findById(id) ?: error("Inserted folder with id=$id not found")
    }

  suspend fun update(id: Long, name: String): Folder = withContext(Dispatchers.IO) {
    dao.update(id, name)
    dao.findById(id) ?: error("Updated folder with id=$id not found")
  }

  suspend fun delete(id: Long): Boolean = withContext(Dispatchers.IO) {
    dao.delete(id) > 0
  }
}
