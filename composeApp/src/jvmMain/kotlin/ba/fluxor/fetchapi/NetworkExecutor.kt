package ba.fluxor.fetchapi

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Global executor backed by Virtual Threads (Project Loom).
 * Every submitted task runs on its own lightweight virtual thread,
 * keeping the UI / main thread completely free.
 */
object NetworkExecutor {

    val executor: ExecutorService =
        Executors.newVirtualThreadPerTaskExecutor()

    val dispatcher: CoroutineDispatcher =
        executor.asCoroutineDispatcher()
}
