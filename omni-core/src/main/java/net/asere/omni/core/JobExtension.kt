package net.asere.omni.core

import kotlinx.coroutines.Job

/**
 * Search recursively for all nested children jobs and join them
 */
suspend fun Job.recursiveJoin() {
    for (job in children) job.recursiveJoin()
    join()
}

/**
 * Join all children jobs (not recursive)
 */
suspend fun Job.joinChildren() {
    for (job in children) job.recursiveJoin()
}

/**
 * Starts all children jobs (not recursive)
 */
fun Job.startChildrenJobs() {
    for (job in children) {
        job.start()
    }
}