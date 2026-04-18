package ba.fluxor.fetchapi.feature.tabs.data

import ba.fluxor.fetchapi.feature.tabs.data.dao.TabDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TabRepository(private val dao: TabDao) {

  suspend fun getAllByProjectId(projectId: Long): List<Tab> = withContext(Dispatchers.IO) {
    dao.findAllByProjectId(projectId)
  }

  suspend fun findByEntity(projectId: Long, type: TabType, entityId: Long): Tab? =
    withContext(Dispatchers.IO) {
      dao.findByEntity(projectId, type, entityId)
    }

  suspend fun create(projectId: Long, type: TabType, entityId: Long): Tab = withContext(Dispatchers.IO) {
    val position = dao.maxPosition(projectId) + 1
    val id = dao.insert(projectId, type, entityId, position)
    Tab(id = id, projectId = projectId, type = type, entityId = entityId, position = position)
  }

  suspend fun delete(id: Long): Boolean = withContext(Dispatchers.IO) {
    dao.delete(id) > 0
  }

  suspend fun deleteByEntity(type: TabType, entityId: Long): Int = withContext(Dispatchers.IO) {
    dao.deleteByEntity(type, entityId)
  }
}
