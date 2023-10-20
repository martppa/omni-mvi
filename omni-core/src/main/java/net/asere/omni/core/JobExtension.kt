package net.asere.omni.core

import kotlinx.coroutines.Job

suspend fun Job.recursiveJoin() {
    for (job in children) job.recursiveJoin()
    join()
}

suspend fun Job.joinChildren() {
    for (job in children) job.recursiveJoin()
}

fun Job.startChildrenJobs() {
    for (job in children) {
        job.start()
    }
}