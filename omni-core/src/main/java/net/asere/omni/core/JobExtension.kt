package net.asere.omni.core

import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Search recursively for all nested children jobs and join them, then join the job itself
 */
suspend fun Job.recursiveJoin() {
    for (job in children) job.recursiveJoin()
    join()
}

/**
 * Search recursively for all nested children jobs and join them
 */
suspend fun Job.recursiveJoinChildren() {
    for (job in children) job.recursiveJoin()
}

/**
 * Await for all children asynchronous completion
 */
suspend fun Job.awaitChildren() {
    coroutineScope {
        children.map { async { it.join() } }.toList().awaitAll()
    }
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
fun Job.startChildren() {
    for (job in children) job.start()
}

/**
 * Starts all children jobs recursively
 */
fun Job.recursiveStartChildren() {
    for (job in children) {
        job.recursiveStart()
    }
}

/**
 * Starts itself and all children jobs recursively
 */
fun Job.recursiveStart() {
    start()
    for (job in children) {
        job.recursiveStart()
    }
}