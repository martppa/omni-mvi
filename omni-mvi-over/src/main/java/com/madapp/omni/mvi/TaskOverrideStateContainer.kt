package com.madapp.omni.mvi

import kotlinx.coroutines.Job

fun <UiState, SideEffect, UiAction> taskOverrideContainer(
    container: Container<UiState, SideEffect, UiAction>
) = TaskOverrideContainer(container)

interface TaskOverrideContainerHost<UiState, SideEffect, UiAction>
    : StateContainerHost<UiState, SideEffect, UiAction> {
    override val container: Container<UiState, SideEffect, UiAction>
}

internal fun <UiState, SideEffect, UiAction>
        Container<UiState, SideEffect, UiAction>.asTaskOverrideContainer() =
    seek<TaskOverrideContainer<*, *, *>> { it is TaskOverrideContainer<*, *, *> }

open class TaskOverrideContainer<UiState, SideEffect, UiAction> internal constructor(
    container: Container<UiState, SideEffect, UiAction>,
) : ContainerDecorator<UiState, SideEffect, UiAction>(
    container
), Container<UiState, SideEffect, UiAction> {
    internal val jobs = mutableMapOf<Any, Job>()
}

@StateHostDsl
fun <UiState, SideEffect, UiAction>
        TaskOverrideContainerHost<UiState, SideEffect, UiAction>.overrideIntent(
    jobId: Any = Unit,
    block: suspend IntentScope<UiState, SideEffect>.() -> Unit
) = intent {
    val jobs = container.asTaskOverrideContainer().jobs
    val job = jobs[jobId]
    job?.cancel()
    job?.join()
    jobs[jobId] = intent { block() }
}