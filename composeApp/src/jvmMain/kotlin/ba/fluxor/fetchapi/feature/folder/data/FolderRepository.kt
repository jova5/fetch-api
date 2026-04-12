package ba.fluxor.fetchapi.feature.folder.data

import ba.fluxor.fetchapi.feature.folder.data.dao.FolderDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FolderRepository(private val dao: FolderDao) {

  suspend fun getAllBySubProjectId(subProjectId: Long): List<Folder> = withContext(Dispatchers.IO) {
    dao.findAllBySubProjectId(subProjectId)
  }

  suspend fun getById(id: Long): Folder? = withContext(Dispatchers.IO) {
    dao.findById(id)
  }

  suspend fun create(subProjectId: Long, name: String): Folder = withContext(Dispatchers.IO) {
    val id = dao.insert(subProjectId, name)
    dao.findById(id) ?: error("Inserted folder with id=$id not found")
  }

  suspend fun update(id: Long, name: String): Folder = withContext(Dispatchers.IO) {
    dao.update(id, name)
    dao.findById(id) ?: error("Updated folder with id=$id not found")
  }

  suspend fun delete(id: Long): Boolean = withContext(Dispatchers.IO) {
    dao.delete(id) > 0
  }

  suspend fun existsByNameAndSubProjectId(name: String, subProjectId: Long): Boolean = withContext(Dispatchers.IO) {
    dao.existsByNameAndSubProjectId(name, subProjectId)
  }

  suspend fun existsByNameAndSubProjectIdAndIdNot(name: String, subProjectId: Long, id: Long): Boolean =
    withContext(Dispatchers.IO) {
      dao.existsByNameAndSubProjectIdAndIdNot(name, subProjectId, id)
    }
}
