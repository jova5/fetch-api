package ba.fluxor.fetchapi.configuration

import java.sql.Connection

/**
 * Runs [block] inside a single database transaction: autocommit is disabled for the duration,
 * the work is committed on success and rolled back on any failure, and the previous autocommit
 * mode is always restored. Use this to make multi-statement writes atomic (DAOs share one
 * [Connection], so callers must serialize transactions — see the shared write `Mutex` in DI).
 */
fun <T> Connection.transaction(block: () -> T): T {
  val previousAutoCommit = autoCommit
  autoCommit = false
  try {
    val result = block()
    commit()
    return result
  } catch (t: Throwable) {
    rollback()
    throw t
  } finally {
    autoCommit = previousAutoCommit
  }
}
