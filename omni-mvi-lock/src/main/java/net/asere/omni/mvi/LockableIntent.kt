package net.asere.omni.mvi

import kotlinx.coroutines.Job

/**
 * Represents an intent that can be locked to prevent concurrent executions of the same action.
 *
 * This class wraps a coroutine [Job] and a manual [locked] flag. It is used by [LockContainer]
 * to keep track of active or restricted intents.
 *
 * @property job The currently running coroutine [Job] for this intent.
 * @property locked A manual override flag to keep the intent locked even after the job finishes.
 */
internal class LockableIntent(
    internal val job: Job,
    internal var locked: Boolean = false
) {
    /**
     * Locks this intent, preventing further executions even if the current job finishes.
     */
    fun lock() {
        locked = true
    }

    /**
     * Unlocks this intent, allowing it to be executed again.
     */
    fun unlock() {
        locked = false
    }
}

/**
 * Extension to check if a [LockableIntent] is currently in a locked state.
 *
 * An intent is considered locked if its associated job is still active or if it has
 * been manually locked.
 *
 * @return `true` if locked, `false` otherwise.
 */
internal fun LockableIntent?.isLocked() = this?.job?.isActive == true || this?.locked == true
