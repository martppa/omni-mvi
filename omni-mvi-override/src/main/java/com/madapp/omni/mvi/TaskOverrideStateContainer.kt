package com.madapp.omni.mvi

import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun <UiState, SideEffect, UiAction> taskOverrideContainer(
    container: Container<UiState, SideEffect, UiAction>
) = TaskOverrideContainer(container)

interface TaskOverrideContainerHost<UiState, SideEffect, UiAction>
    : StateContainerHost<UiState, SideEffect, UiAction> {
    override val container: Container<UiState, SideEffect, UiAction>
}

internal fun <UiState, SideEffect, UiAction>
        Container<UiState, SideEffect, UiAction>.asTaskOverrideContainer() =
    seek<TaskOverrideContainer<UiState, SideEffect, UiAction>> {
        it is TaskOverrideContainer<*, *, *>
    }

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

@StateHostDsl
fun <UiState, SideEffect, UiAction>
        TaskOverrideContainerHost<UiState, SideEffect, UiAction>.overrideIntent(
    intentId: Any = Unit,
    block: suspend IntentScope<UiState, SideEffect>.() -> Unit
) = container.asTaskOverrideContainer().overrideIntent(intentId, block)