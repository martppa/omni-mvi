package com.madapp.omni.mvi

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun <UiState, SideEffect, UiAction> lockContainer(
    container: Container<UiState, SideEffect, UiAction>
) = LockContainer(container)

interface LockContainerHost<UiState, SideEffect, UiAction>
    : StateContainerHost<UiState, SideEffect, UiAction> {
    override val container: Container<UiState, SideEffect, UiAction>
}

internal fun <UiState, SideEffect, UiAction>
        Container<UiState, SideEffect, UiAction>.asLockContainer() =
    seek<LockContainer<UiState, SideEffect, UiAction>> { it is LockContainer<*, *, *> }

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

fun <UiState, SideEffect, UiAction>
        LockContainerHost<UiState, SideEffect, UiAction>.cancelIntent(
    intentId: Any = Unit,
) = container.asLockContainer().cancelIntent(intentId)

fun <UiState, SideEffect, UiAction>
        LockContainerHost<UiState, SideEffect, UiAction>.lockIntent(
    intentId: Any = Unit
) = container.asLockContainer().lockIntent(intentId)

fun <UiState, SideEffect, UiAction>
        LockContainerHost<UiState, SideEffect, UiAction>.unlockIntent(
    intentId: Any = Unit
) = container.asLockContainer().unlockIntent(intentId)

@StateHostDsl
fun <UiState, SideEffect, UiAction>
        LockContainerHost<UiState, SideEffect, UiAction>.lockIntent(
    intentId: Any = Unit,
    block: suspend IntentScope<UiState, SideEffect>.() -> Unit
) = intent {
    container.asLockContainer().lockIntent(intentId, block)
}

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