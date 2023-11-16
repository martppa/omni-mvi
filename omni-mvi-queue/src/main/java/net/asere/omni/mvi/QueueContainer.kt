package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach

/**
 * This is a state container capable of enqueuing intents. Each invoked intent
 * is placed into a queue of execution.
 */
open class QueueContainer<State, Effect> internal constructor(
    override val container: ExposedStateContainer<State, Effect>,
) : StateContainerDecorator<State, Effect>(
    container
), ExposedStateContainer<State, Effect>,
    QueueContainerHost<State, Effect> {

    private lateinit var intentQueue: Channel<Job>
    private lateinit var consumeJob: Job

    init {
        startIntentQueue()
    }


    private fun startIntentQueue() {
        intentQueue = Channel(capacity = Channel.UNLIMITED)
        consumeJob = intentJob {
            intentQueue.consumeEach { consumeIntent(it).join() }
        }
    }

    private fun consumeIntent(job: Job) = intentJob { job.join() }

    internal fun clearQueue() = intent {
        consumeJob.cancel()
        consumeJob.join()
        intentQueue.cancel()
    }

    /**
     * Enqueue an intent
     *
     * @param block intent's content
     */
    internal fun enqueue(
        block: suspend IntentScope<State, Effect>.() -> Unit
    ) = intent {
        if (intentQueue.isClosed()) startIntentQueue()
        intentQueue.send(
            intentJob(start = CoroutineStart.LAZY) { block() }
        )
    }
}

fun <State, Effect> queueContainer(
    container: ExposedStateContainer<State, Effect>
) = QueueContainer(container)

fun <State, Effect> ExposedStateContainer<State, Effect>
        .buildQueueContainer() = queueContainer(this)

internal fun <State, Effect>
        ExposedStateContainer<State, Effect>.asQueueContainer() =
    asStateContainer().seek<QueueContainer<State, Effect>> { it is QueueContainer<*, *> }