package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.suspendCoroutine

fun <State, Effect> Container<State, Effect>.asExecutableContainer(): ExecutableContainer =
    seek { it is ExecutableContainer }

suspend fun <State, Effect>
        StateContainerHost<State, Effect>.awaitJobs() =
    container.asExecutableContainer().awaitJobs()

suspend fun <State, Effect>
        StateContainer<State, Effect>.awaitJobs(until: () -> Boolean) {
    val emptyScope = CoroutineScope(EmptyCoroutineContext)
    val awaitingJob = emptyScope.launch(start = CoroutineStart.LAZY) {
        asExecutableContainer().awaitJobs()
    }
    suspendCoroutine<Unit> { continuation ->
        fun verify() {
            if (until()) {
                awaitingJob.cancelChildren()
                awaitingJob.cancel()
            }
        }
        asDelegatorContainer().delegate(
            doOnAnyEmission(
                container = this@awaitJobs,
                block = ::verify
            )
        )
        emptyScope.launch {
            awaitingJob.join()
            continuation.resumeWith(Result.success(Unit))
        }
    }
}

private fun <State, Effect> doOnAnyEmission(
    container: StateContainer<State, Effect>,
    block: () -> Unit
): StateContainer<State, Effect> {
    return object : DelegatorContainer<State, Effect>(container) {
        override fun update(function: State.() -> State) {
            super.update(function)
            block()
        }

        override fun post(effect: Effect) {
            super.post(effect)
            block()
        }
    }
}

fun <State, Effect>
        StateContainerHost<State, Effect>.launchJobs() =
    container.asExecutableContainer().launchJobs()

fun <State, Effect>
        StateContainerHost<State, Effect>.releaseExecution() =
    container.asExecutableContainer().releaseExecution()

fun <State, Effect>
        StateContainerHost<State, Effect>.lockExecution() =
    container.asExecutableContainer().lockExecution()

fun <State, Effect> Container<State, Effect>.asDelegatorContainer(): DelegatorContainer<State, Effect> =
    seek { it is DelegatorContainer<*, *> }

fun <State, Effect> Container<State, Effect>.delegate(
    container: StateContainer<State, Effect>
) = asDelegatorContainer().delegate(container)

fun <State, Effect> Container<State, Effect>.clearDelegate() =
    asDelegatorContainer().clearDelegate()

fun <State, Effect> StateContainerHost<State, Effect>.delegate(
    container: StateContainer<State, Effect>
) = this.container.delegate(container)

fun <State, Effect> StateContainerHost<State, Effect>.clearDelegate() =
    container.clearDelegate()