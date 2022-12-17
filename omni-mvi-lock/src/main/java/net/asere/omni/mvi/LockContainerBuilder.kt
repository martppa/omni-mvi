package net.asere.omni.mvi

fun <State, Effect> lockContainer(
    container: Container<State, Effect>
) = LockContainer(container)