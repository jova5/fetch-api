package ba.fluxor.fetchapi.feature.sub_project.data

import ba.fluxor.fetchapi.feature.sub_project.data.dao.SubProjectVariableDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubProjectVariableRepository(private val dao: SubProjectVariableDao) {

  suspend fun getAllBySubProjectId(subProjectId: Long): List<SubProjectVariable> = withContext(Dispatchers.IO) {
    dao.findAllBySubProjectId(subProjectId)
  }

  suspend fun replaceAll(subProjectId: Long, items: List<SubProjectVariable>) = withContext(Dispatchers.IO) {
    dao.replaceAllForSubProject(subProjectId, items)
  }
}
