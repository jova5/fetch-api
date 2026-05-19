package ba.fluxor.fetchapi.feature.sub_project.data

import ba.fluxor.fetchapi.feature.sub_project.data.dao.SubProjectDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubProjectRepository(private val dao: SubProjectDao) {

  suspend fun getAllByProjectId(projectId: Long): List<SubProject> = withContext(Dispatchers.IO) {
    dao.findAllByProjectId(projectId)
  }

  suspend fun getById(id: Long): SubProject? = withContext(Dispatchers.IO) {
    dao.findById(id)
  }

  suspend fun create(projectId: Long, name: String, authType: String = "NONE", authConfig: String? = null): SubProject =
    withContext(Dispatchers.IO) {
      val id = dao.insert(projectId, name, authType, authConfig)
      dao.findById(id) ?: error("Inserted sub_project with id=$id not found")
    }

  suspend fun update(id: Long, name: String, authType: String, authConfig: String?): SubProject =
    withContext(Dispatchers.IO) {
      dao.update(id, name, authType, authConfig)
      dao.findById(id) ?: error("Updated sub_project with id=$id not found")
    }

  suspend fun delete(id: Long): Boolean = withContext(Dispatchers.IO) {
    dao.delete(id) > 0
  }
}
