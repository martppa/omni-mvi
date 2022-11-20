package net.asere.omni.mvi

internal fun <State, Effect, Action>
        Container<State, Effect, Action>.asLockContainer() =
    seek<LockContainer<State, Effect, Action>> { it is LockContainer<*, *, *> }