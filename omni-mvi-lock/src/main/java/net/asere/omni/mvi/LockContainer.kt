package net.asere.omni.mvi

import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

open class LockContainer<State, Effect> internal constructor(
    override val container: ExposedStateContainer<State, Effect>,
) : StateContainerDecorator<State, Effect>(
    container
), Container,
    LockContainerHost<State, Effect> {

    private val mutex = Mutex()
    private val intents = mutableMapOf<Any, LockableIntent>()

    internal fun cancelIntent(intentId: Any) = intent {
        mutex.withLock {
            val job = intents[intentId]?.job
            job?.cancelChildren()
            job?.cancel()
            job?.join()
            intents.remove(intentId)
        }
    }

    internal fun lockIntent(
        intentId: Any,
        block: suspend StateIntentScope<State, Effect>.() -> Unit
    ) = intent {
        if (!intents[intentId].isLocked()) {
            intents[intentId] = LockableIntent(intent { block() })
        }
    }

    internal fun lockIntent(intentId: Any) = intent {
        mutex.withLock { intents[intentId]?.lock() }
    }

    internal fun unlockIntent(intentId: Any) = intent {
        mutex.withLock { intents[intentId]?.unlock() }
    }
}

fun <State, Effect> lockContainer(
    container: ExposedStateContainer<State, Effect>
) = LockContainer(container)

fun <State, Effect> ExposedStateContainer<State, Effect>
        .buildLockContainer() = lockContainer(this)

internal fun <State, Effect>
        ExposedStateContainer<State, Effect>.asLockContainer() =
    asStateContainer().seek<LockContainer<State, Effect>> { it is LockContainer<*, *> }