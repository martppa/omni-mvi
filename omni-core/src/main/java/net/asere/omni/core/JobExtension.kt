package net.asere.omni.core

import kotlinx.coroutines.Job

/**
 * Suspends the current coroutine until all direct child jobs of this [Job] have completed.
 *
 * This operation is not recursive; it only waits for immediate children to finish.
 */
suspend fun Job.joinChildren() {
    for (job in children) job.join()
}

/**
 * Starts all direct child jobs of this [Job] that are in a [kotlinx.coroutines.CoroutineStart.LAZY] state.
 *
 * This operation is not recursive; it only starts immediate children.
 */
fun Job.startChildren() {
    for (job in children) job.start()
}
