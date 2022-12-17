package net.asere.omni.mvi

fun <State, Effect> Container<State, Effect>
        .asExecutableContainer(): ExecutableContainer =
    seek { it is ExecutableContainer }

suspend fun <State, Effect>
        StateContainerHost<State, Effect>.awaitJobs() =
    container.asExecutableContainer().awaitJobs()

fun <State, Effect>
        StateContainerHost<State, Effect>.launchJobs() =
    container.asExecutableContainer().launchJobs()

fun <State, Effect>
        StateContainerHost<State, Effect>.releaseExecution() =
    container.asExecutableContainer().releaseExecution()

fun <State, Effect>
        StateContainerHost<State, Effect>.lockExecution() =
    container.asExecutableContainer().lockExecution()

fun <State, Effect> Container<State, Effect>
        .asDelegatorContainer(): DelegatorContainer<State, Effect> =
    seek { it is DelegatorContainer<*, *> }

fun <State, Effect> Container<State, Effect>.delegate(
    container: StateContainer<State, Effect>
) = asDelegatorContainer().delegate(container)

fun <State, Effect> Container<State, Effect>.clearDelegate() =
    asDelegatorContainer().clearDelegate()

fun <State, Effect> StateContainerHost<State, Effect>.delegate(
    container: StateContainer<State, Effect>
) = container.delegate(container)

fun <State, Effect> StateContainerHost<State, Effect>.clearDelegate() =
    container.clearDelegate()