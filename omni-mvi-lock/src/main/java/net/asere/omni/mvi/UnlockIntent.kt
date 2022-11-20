package net.asere.omni.mvi

fun <State, Effect, Action>
        LockContainerHost<State, Effect, Action>.unlockIntent(
    intentId: Any = Unit
) = container.asLockContainer().unlockIntent(intentId)