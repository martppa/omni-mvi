package net.asere.omni.mvi

fun <State, Effect, Action> lockContainer(
    container: Container<State, Effect, Action>
) = LockContainer(container)