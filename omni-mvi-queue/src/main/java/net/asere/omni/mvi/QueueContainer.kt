package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach

open class QueueContainer<State, Effect> internal constructor(
    override val container: Container<State, Effect>,
) : ContainerDecorator<State, Effect>(
    container
), Container<State, Effect>,
    QueueContainerHost<State, Effect> {

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
        block: suspend IntentScope<State, Effect>.() -> Unit
    ) = intent {
        if (intentQueue.isClosed()) startIntentQueue()
        intentQueue.send(
            intent(start = CoroutineStart.LAZY) { block() }
        )
    }
}

fun <State, Effect> queueContainer(
    container: Container<State, Effect>
) = QueueContainer(container)

fun <State, Effect> Container<State, Effect>
        .buildQueueContainer() = queueContainer(this)

internal fun <State, Effect>
        Container<State, Effect>.asQueueContainer() =
    seek<QueueContainer<State, Effect>> { it is QueueContainer<*, *> }