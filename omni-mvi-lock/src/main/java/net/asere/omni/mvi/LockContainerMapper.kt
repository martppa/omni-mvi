package net.asere.omni.mvi

internal fun <State, Effect>
        Container<State, Effect>.asLockContainer() =
    seek<LockContainer<State, Effect>> { it is LockContainer<*, *> }