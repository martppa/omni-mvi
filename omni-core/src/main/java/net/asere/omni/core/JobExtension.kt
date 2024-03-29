package net.asere.omni.core

import kotlinx.coroutines.Job

/**
 * Search recursively for all nested children jobs and join them
 */
suspend fun Job.recursiveJoinChildren() {
    suspend fun Job.recursiveJoin() {
        for (job in children) job.recursiveJoin()
        join()
    }
    for (job in children) job.recursiveJoin()
}

/**
 * Join all children jobs (not recursive)
 */
suspend fun Job.joinChildren() {
    for (job in children) job.join()
}

/**
 * Starts all children jobs (not recursive)
 */
fun Job.startChildrenJobs() {
    for (job in children) {
        job.start()
    }
}