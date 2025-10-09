package net.asere.omni.core

import kotlinx.coroutines.Job

/**
 * Join all children jobs (not recursive)
 */
suspend fun Job.joinChildren() {
    for (job in children) job.join()
}

/**
 * Starts all children jobs (not recursive)
 */
fun Job.startChildren() {
    for (job in children) job.start()
}
