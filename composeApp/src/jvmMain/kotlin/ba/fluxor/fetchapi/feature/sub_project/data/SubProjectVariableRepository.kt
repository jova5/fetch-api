package ba.fluxor.fetchapi.feature.sub_project.data

import ba.fluxor.fetchapi.feature.sub_project.data.dao.SubProjectVariableDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class SubProjectVariableRepository(
  private val dao: SubProjectVariableDao,
  private val dbMutex: Mutex,
) {

  suspend fun getAllBySubProjectId(subProjectId: Long): List<SubProjectVariable> = withContext(Dispatchers.IO) {
    dao.findAllBySubProjectId(subProjectId)
  }

  // Guarded by the shared write mutex: this is a transaction, so it must not interleave with the
  // tree-reorder transactions on the single shared connection.
  suspend fun replaceAll(subProjectId: Long, items: List<SubProjectVariable>) = dbMutex.withLock {
    withContext(Dispatchers.IO) {
      dao.replaceAllForSubProject(subProjectId, items)
    }
  }
}
