package com.madapp.omni.mvi

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach

open class QueueContainer<UiState, SideEffect, UiAction> internal constructor(
    override val container: Container<UiState, SideEffect, UiAction>,
) : ContainerDecorator<UiState, SideEffect, UiAction>(
    container
), Container<UiState, SideEffect, UiAction>,
    QueueContainerHost<UiState, SideEffect, UiAction> {

    private lateinit var intentQueue: Channel<Job>
    private lateinit var consumeJob: Job

    init {
        startIntentQueue()
    }

    private fun startIntentQueue() {
        intentQueue = Channel(capacity = Channel.UNLIMITED)
        consumeJob = intent {
            intentQueue.consumeEach { consumeIntent(it).join() }
        }
    }

    private fun consumeIntent(job: Job) = intent { job.join() }

    internal fun clearQueue() = intent {
        consumeJob.cancel()
        consumeJob.join()
        intentQueue.cancel()
    }

    internal fun enqueue(
        block: suspend IntentScope<UiState, SideEffect>.() -> Unit
    ) = intent {
        if (intentQueue.isClosed()) startIntentQueue()
        intentQueue.send(
            intent(start = CoroutineStart.LAZY) { block() }
        )
    }
}