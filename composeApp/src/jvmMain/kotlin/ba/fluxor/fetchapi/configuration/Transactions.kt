package ba.fluxor.fetchapi.configuration

import java.sql.Connection

/**
 * Runs [block] inside a single database transaction: autocommit is disabled for the duration,
 * the work is committed on success and rolled back on any failure, and the previous autocommit
 * mode is always restored. Use this to make multi-statement writes atomic (DAOs share one
 * [Connection], so callers must serialize transactions — see the shared write `Mutex` in DI).
 *
 * Known limitation (acceptable for this single-user desktop app): [autoCommit] is connection-wide,
 * and only the transactional call sites take the shared `Mutex`. A plain (non-transactional) write
 * running concurrently on another thread could be swept into this transaction's commit/rollback
 * boundary. The real-world window is tiny (writes are user-driven, one at a time), so we accept it
 * rather than serialize every write. To eliminate it, confine all DB access to a single-thread
 * dispatcher (`Dispatchers.IO.limitedParallelism(1)`), which would also make the `Mutex` redundant.
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
