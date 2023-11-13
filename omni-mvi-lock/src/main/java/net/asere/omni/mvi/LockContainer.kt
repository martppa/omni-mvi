package net.asere.omni.mvi

import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.asere.omni.core.Container

/**
 * This container lets you control intents execution by locking them. Locking an intent
 * means that the same intent can't be execute again until previous execution ends.
 */
open class LockContainer<State, Effect> internal constructor(
    override val container: ExposedStateContainer<State, Effect>,
) : StateContainerDecorator<State, Effect>(
    container
), Container,
    LockContainerHost<State, Effect> {

    private val mutex = Mutex()
    private val intents = mutableMapOf<Any, LockableIntent>()

    /**
     * Cancels any intent identified with the provided id value.
     */
    internal fun cancelIntent(intentId: Any) = intent {
        mutex.withLock {
            val job = intents[intentId]?.job
            job?.cancelChildren()
            job?.cancel()
            job?.join()
            intents.remove(intentId)
        }
    }

    /**
     * Launches a lock intent
     */
    internal fun lockIntent(
        intentId: Any,
        block: suspend IntentScope<State, Effect>.() -> Unit
    ) = intent {
        if (!intents[intentId].isLocked()) {
            intents[intentId] = LockableIntent(intent { block() })
        }
    }

    /**
     * Locks any intent identified with the provided id value, preventing it from
     * being executed.
     */
    internal fun lockIntent(intentId: Any) = intent {
        mutex.withLock { intents[intentId]?.lock() }
    }

    /**
     * Unlock an intent allowing it to resume executions
     */
    internal fun unlockIntent(intentId: Any) = intent {
        mutex.withLock { intents[intentId]?.unlock() }
    }
}

internal fun <State, Effect> lockContainer(
    container: ExposedStateContainer<State, Effect>
) = LockContainer(container)

/**
 * Turns this container into a lock container
 */
fun <State, Effect> ExposedStateContainer<State, Effect>
        .buildLockContainer() = lockContainer(this)

/**
 * Seeks for a LockContainer from inside a decorated container
 */
internal fun <State, Effect>
        ExposedStateContainer<State, Effect>.asLockContainer() =
    asStateContainer().seek<LockContainer<State, Effect>> { it is LockContainer<*, *> }