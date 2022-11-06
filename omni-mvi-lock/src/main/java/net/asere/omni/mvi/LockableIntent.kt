package net.asere.omni.mvi

import kotlinx.coroutines.Job

internal class LockableIntent(
    internal val job: Job,
    internal var locked: Boolean = false
) {
    fun lock() {
        locked = true
    }
    fun unlock() {
        locked = false
    }
}

internal fun LockableIntent?.isLocked() = this?.job?.isActive == true || this?.locked == true