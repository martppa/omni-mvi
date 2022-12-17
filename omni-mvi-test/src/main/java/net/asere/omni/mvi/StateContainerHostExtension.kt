package net.asere.omni.mvi

fun <State, Effect, Action> Container<State, Effect, Action>
        .asExecutableContainer(): ExecutableContainer =
    seek { it is ExecutableContainer }

suspend fun <State, Effect, Action>
        StateContainerHost<State, Effect, Action>.awaitJobs() =
    container.asExecutableContainer().awaitJobs()

fun <State, Effect, Action>
        StateContainerHost<State, Effect, Action>.launchJobs() =
    container.asExecutableContainer().launchJobs()

fun <State, Effect, Action>
        StateContainerHost<State, Effect, Action>.releaseExecution() =
    container.asExecutableContainer().releaseExecution()

fun <State, Effect, Action>
        StateContainerHost<State, Effect, Action>.lockExecution() =
    container.asExecutableContainer().lockExecution()

fun <State, Effect, Action> Container<State, Effect, Action>
        .asDelegatorContainer(): DelegatorContainer<State, Effect, Action> =
    seek { it is DelegatorContainer<*, *, *> }

fun <State, Effect, Action> Container<State, Effect, Action>.delegate(
    container: StateContainer<State, Effect, Action>
) = asDelegatorContainer().delegate(container)

fun <State, Effect, Action> Container<State, Effect, Action>.clearDelegate() =
    asDelegatorContainer().clearDelegate()

fun <State, Effect, Action> StateContainerHost<State, Effect, Action>.delegate(
    container: StateContainer<State, Effect, Action>
) = container.delegate(container)

fun <State, Effect, Action> StateContainerHost<State, Effect, Action>.clearDelegate() =
    container.clearDelegate()