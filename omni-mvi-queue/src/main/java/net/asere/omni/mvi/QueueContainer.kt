package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach

/**
 * A specialized [StateContainerDecorator] that executes intents sequentially in a queue.
 *
 * When an intent is enqueued, it is added to an internal [Channel]. A dedicated consumer
 * coroutine processes these intents one by one, ensuring that an intent only starts after
 * the previous one has completely finished. This is useful for maintaining strict
 * execution order for related actions.
 *
 * @param State The type of the UI state.
 * @param Effect The type of the side effect.
 * @property container The inner [StateContainer] to be decorated with queuing capabilities.
 */
open class QueueContainer<State : Any, Effect : Any> internal constructor(
    override val container: StateContainer<State, Effect>,
) : StateContainerDecorator<State, Effect>(
    container
), StateContainer<State, Effect>,
    QueueContainerHost<State, Effect> {

    private lateinit var intentQueue: Channel<Job>
    private lateinit var consumeJob: Job

    init {
        startIntentQueue()
    }

    /**
     * Initializes the queue and starts the consumer coroutine.
     */
    private fun startIntentQueue() {
        intentQueue = Channel(capacity = Channel.UNLIMITED)
        consumeJob = intentJob {
            intentQueue.consumeEach { consumeIntent(it).join() }
        }
    }

    /**
     * Joins the provided [job], effectively waiting for its completion within the queue.
     */
    private fun consumeIntent(job: Job) = intentJob { job.join() }

    /**
     * Cancels the current queue processing and the queue itself.
     *
     * Active intents will be canceled, and the consumer job will be stopped.
     */
    internal fun clearQueue() = intent {
        consumeJob.cancel()
        consumeJob.join()
        intentQueue.cancel()
    }

    /**
     * Enqueues a new intent for sequential execution.
     *
     * If the queue was previously cleared or closed, it will be automatically restarted.
     *
     * @param block The suspendable logic to be executed.
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

/**
 * Internal factory function to create a [QueueContainer].
 */
fun <State : Any, Effect : Any> queueContainer(
    container: StateContainer<State, Effect>
) = QueueContainer(container)

/**
 * Extension to wrap an existing [StateContainer] into a [QueueContainer].
 *
 * @return A new [QueueContainer] instance decorating the original one.
 */
fun <State : Any, Effect : Any> StateContainer<State, Effect>
        .buildQueueContainer() = queueContainer(this)

/**
 * Searches the decoration chain for a [QueueContainer].
 *
 * @return The [QueueContainer] found in the stack.
 * @throws RuntimeException if no [QueueContainer] is found.
 */
internal fun <State : Any, Effect : Any>
        StateContainer<State, Effect>.asQueueContainer() =
    asStateContainer().seek<QueueContainer<State, Effect>> { it is QueueContainer<*, *> }
