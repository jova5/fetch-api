package ba.fluxor.fetchapi.feature.project_tree.data

import ba.fluxor.fetchapi.configuration.transaction
import ba.fluxor.fetchapi.feature.folder.data.dao.FolderDao
import ba.fluxor.fetchapi.feature.request.data.dao.RequestDao
import ba.fluxor.fetchapi.feature.sub_project.data.dao.SubProjectDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.sql.Connection

/**
 * Coordinates the tree's drag-and-drop moves as single atomic transactions. A folder move spans
 * two tables (folder + request), so it cannot be owned by either feature alone; this data-layer
 * service is the cross-aggregate coordinator. It opens one transaction and delegates the actual
 * SQL to each feature's DAO, so request SQL still lives in [RequestDao], folder SQL in [FolderDao],
 * etc. The shared [dbMutex] serializes transactions because every DAO shares one [Connection].
 *
 * Each method takes the destination sibling order already computed by the caller (with the dragged
 * id at its target index); persisting that order is the source of truth for `position`.
 */
class TreeReorderService(
  private val connection: Connection,
  private val folderDao: FolderDao,
  private val requestDao: RequestDao,
  private val subProjectDao: SubProjectDao,
  private val dbMutex: Mutex,
) {

  suspend fun moveFolder(
    draggedId: Long,
    targetSubProjectId: Long,
    targetParentFolderId: Long?,
    orderedSiblingIds: List<Long>,
  ) = runInTransaction {
    val descendantFolderIds = folderDao.move(draggedId, targetSubProjectId, targetParentFolderId)
    requestDao.restampSubProject(descendantFolderIds, targetSubProjectId)
    folderDao.updatePositions(orderedSiblingIds)
  }

  suspend fun moveRequest(
    draggedId: Long,
    targetSubProjectId: Long,
    targetFolderId: Long?,
    orderedSiblingIds: List<Long>,
  ) = runInTransaction {
    requestDao.move(draggedId, targetSubProjectId, targetFolderId)
    requestDao.updatePositions(orderedSiblingIds)
  }

  suspend fun reorderSubProjects(orderedIds: List<Long>) = runInTransaction {
    subProjectDao.updatePositions(orderedIds)
  }

  private suspend fun runInTransaction(block: () -> Unit) =
    dbMutex.withLock {
      withContext(Dispatchers.IO) {
        connection.transaction(block)
      }
    }
}
