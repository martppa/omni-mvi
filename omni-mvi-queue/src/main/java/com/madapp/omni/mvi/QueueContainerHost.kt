package com.madapp.omni.mvi

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

fun <UiState, SideEffect, UiAction> queueContainer(
    container: Container<UiState, SideEffect, UiAction>
) = QueueContainer(container)

interface QueueContainerHost<UiState, SideEffect, UiAction>
    : StateContainerHost<UiState, SideEffect, UiAction> {
    override val container: Container<UiState, SideEffect, UiAction>
}

internal fun <UiState, SideEffect, UiAction>
        Container<UiState, SideEffect, UiAction>.asQueueContainer() =
    seek<QueueContainer<*, *, *>> { it is QueueContainer<*, *, *> }

open class QueueContainer<UiState, SideEffect, UiAction> internal constructor(
    container: Container<UiState, SideEffect, UiAction>,
) : ContainerDecorator<UiState, SideEffect, UiAction>(
    container
), Container<UiState, SideEffect, UiAction> {

    internal lateinit var jobQueue: Channel<Job>
    internal lateinit var consumeJob: Job

    init {
        buildQueue()
        consumeQueue()
    }

    private fun buildQueue() {
        jobQueue = Channel(capacity = Channel.UNLIMITED)
    }

    private fun consumeQueue() {
        consumeJob = coroutineScope.launch {
            jobQueue.consumeEach { it.join() }
        }
    }

    internal suspend fun clearQueue() {
        consumeJob.cancel()
        consumeJob.join()
        jobQueue.cancel()
    }
}

fun <UiState, SideEffect, UiAction>
        QueueContainerHost<UiState, SideEffect, UiAction>.clearQueue() = intent {
    container.asQueueContainer().clearQueue()
}

@StateHostDsl
fun <UiState, SideEffect, UiAction>
        QueueContainerHost<UiState, SideEffect, UiAction>.queuedIntent(
    block: suspend IntentScope<UiState, SideEffect>.() -> Unit
) = intent {
    container.asQueueContainer().jobQueue.send(
        intent(start = CoroutineStart.LAZY) { block() }
    )
}