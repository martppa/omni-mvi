package com.madapp.omni.mvi

import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

open class TaskOverrideContainer<UiState, SideEffect, UiAction> internal constructor(
    override val container: Container<UiState, SideEffect, UiAction>,
) : ContainerDecorator<UiState, SideEffect, UiAction>(
    container
), Container<UiState, SideEffect, UiAction>,
    TaskOverrideContainerHost<UiState, SideEffect, UiAction> {

    private val mutex = Mutex()
    private val intents = mutableMapOf<Any, Job>()

    internal fun overrideIntent(
        intentId: Any = Unit,
        block: suspend IntentScope<UiState, SideEffect>.() -> Unit
    ) = intent {
        mutex.withLock {
            val job = intents[intentId]
            job?.cancel()
            job?.join()
            intents[intentId] = intent { block() }
        }
    }
}