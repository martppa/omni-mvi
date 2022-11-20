package net.asere.omni.mvi

fun <State, Effect, Action>
        LockContainerHost<State, Effect, Action>.cancelIntent(
    intentId: Any = Unit,
) = container.asLockContainer().cancelIntent(intentId)