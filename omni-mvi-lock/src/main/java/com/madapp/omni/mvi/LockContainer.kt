package com.madapp.omni.mvi

import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

open class LockContainer<UiState, SideEffect, UiAction> internal constructor(
    override val container: Container<UiState, SideEffect, UiAction>,
) : ContainerDecorator<UiState, SideEffect, UiAction>(
    container
), Container<UiState, SideEffect, UiAction>,
    LockContainerHost<UiState, SideEffect, UiAction> {

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
        block: suspend IntentScope<UiState, SideEffect>.() -> Unit
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