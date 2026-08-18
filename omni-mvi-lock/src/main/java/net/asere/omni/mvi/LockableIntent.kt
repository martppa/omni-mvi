package net.asere.omni.mvi

import kotlinx.coroutines.InternalCoroutinesApi

/**
 * Represents an intent that can be locked to prevent concurrent executions of the same action.
 *
 * This class wraps an [Intent] and a manual [locked] flag. It is used by [LockContainer]
 * to keep track of active or restricted intents.
 *
 * @property intent The underlying [Intent] for this execution.
 * @property locked A manual override flag to keep the intent locked even after the execution finishes.
 */
@OptIn(InternalCoroutinesApi::class)
internal class LockableIntent(
    internal val intent: Intent,
    internal var locked: Boolean = false
) : Intent by intent {
    /**
     * Locks this intent, preventing further executions even if the current intent finishes.
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
 * An intent is considered locked if its associated execution is still active or if it has
 * been manually locked.
 *
 * @return `true` if locked, `false` otherwise.
 */
internal fun LockableIntent?.isLocked() = this?.isActive == true || this?.locked == true
