package net.asere.omni.mvi

import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.asere.omni.core.Container

/**
 * A specialized [StateContainerDecorator] that provides control over intent execution via locking.
 *
 * Locking an intent means that subsequent attempts to execute the same intent (identified by an ID)
 * will be ignored until the previous execution finishes or the intent is manually unlocked.
 * This is useful for preventing "double-tap" actions or redundant network requests.
 *
 * @param State The type of the UI state.
 * @param Effect The type of the side effect.
 * @property container The inner [StateContainer] to be decorated with locking capabilities.
 */
open class LockContainer<State : Any, Effect : Any> internal constructor(
    override val container: StateContainer<State, Effect>,
) : StateContainerDecorator<State, Effect>(
    container
), Container,
    LockContainerHost<State, Effect> {

    private val mutex = Mutex()
    private val intents = mutableMapOf<Any, LockableIntent>()

    /**
     * Cancels any active intent identified by [intentId].
     *
     * This will cancel the coroutine job and its children, then wait for completion
     * before removing the intent from the tracked map.
     *
     * @param intentId The identifier for the intent to cancel.
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
     * Executes a block of code as a locked intent.
     *
     * If an intent with the same [intentId] is already locked or running, the [block]
     * will not be executed.
     *
     * @param intentId The identifier for this intent.
     * @param block The logic to execute if not locked.
     */
    internal fun lockIntent(
        intentId: Any,
        block: suspend IntentScope<State, Effect>.() -> Unit
    ) = intent {
        if (!intents[intentId].isLocked()) {
            intents[intentId] = LockableIntent(intentJob { block() })
        }
    }

    /**
     * Manually locks the intent identified by [intentId], preventing it from being executed
     * until [unlockIntent] is called.
     *
     * @param intentId The identifier to lock.
     */
    internal fun lockIntent(intentId: Any) = intent {
        mutex.withLock { intents[intentId]?.lock() }
    }

    /**
     * Unlocks the intent identified by [intentId], allowing future executions.
     *
     * @param intentId The identifier to unlock.
     */
    internal fun unlockIntent(intentId: Any) = intent {
        mutex.withLock { intents[intentId]?.unlock() }
    }
}

/**
 * Internal factory function to create a [LockContainer].
 */
internal fun <State : Any, Effect : Any> lockContainer(
    container: StateContainer<State, Effect>
) = LockContainer(container)

/**
 * Extension to wrap an existing [StateContainer] into a [LockContainer].
 *
 * @return A new [LockContainer] instance decorating the original one.
 */
fun <State : Any, Effect : Any> StateContainer<State, Effect>
        .buildLockContainer() = lockContainer(this)

/**
 * Searches the decoration chain for a [LockContainer].
 *
 * @return The [LockContainer] found in the stack.
 * @throws RuntimeException if no [LockContainer] is found.
 */
internal fun <State : Any, Effect : Any>
        StateContainer<State, Effect>.asLockContainer() =
    asStateContainer().seek<LockContainer<State, Effect>> { it is LockContainer<*, *> }
