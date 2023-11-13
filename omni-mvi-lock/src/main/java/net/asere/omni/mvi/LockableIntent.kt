package net.asere.omni.mvi

import kotlinx.coroutines.Job

/**
 * Type of intent that allows execution blocking
 *
 * @param job Running job
 * @param locked defines whether is the intent blocked or not
 */
internal class LockableIntent(
    internal val job: Job,
    internal var locked: Boolean = false
) {
    /**
     * Lock this intent and avoid it to be overridden
     */
    fun lock() {
        locked = true
    }

    /**
     * Release this intent and allow it to be overridden
     */
    fun unlock() {
        locked = false
    }
}

/**
 * Returns whether or not the intent is locked
 */
internal fun LockableIntent?.isLocked() = this?.job?.isActive == true || this?.locked == true