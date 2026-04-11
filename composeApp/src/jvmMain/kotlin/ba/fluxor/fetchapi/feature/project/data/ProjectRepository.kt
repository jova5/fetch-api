package ba.fluxor.fetchapi.feature.project.data

import ba.fluxor.fetchapi.feature.project.data.dao.ProjectDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProjectRepository(private val dao: ProjectDao) {

  suspend fun getAll(): List<Project> = withContext(Dispatchers.IO) {
    dao.findAll()
  }

  suspend fun getById(id: Long): Project? = withContext(Dispatchers.IO) {
    dao.findById(id)
  }

  suspend fun create(name: String): Project = withContext(Dispatchers.IO) {
    val id = dao.insert(name)
    dao.findById(id) ?: error("Inserted project with id=$id not found")
  }

  suspend fun update(id: Long, name: String): Project = withContext(Dispatchers.IO) {
    dao.update(id, name)
    dao.findById(id) ?: error("Updated project with id=$id not found")
  }

  suspend fun delete(id: Long): Boolean = withContext(Dispatchers.IO) {
    dao.delete(id) > 0
  }
}
