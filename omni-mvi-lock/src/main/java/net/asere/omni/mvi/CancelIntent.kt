package net.asere.omni.mvi

fun <State, Effect>
        LockContainerHost<State, Effect>.cancelIntent(
    intentId: Any = Unit,
) = container.asLockContainer().cancelIntent(intentId)