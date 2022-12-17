package net.asere.omni.mvi

fun <State, Effect>
        LockContainerHost<State, Effect>.unlockIntent(
    intentId: Any = Unit
) = container.asLockContainer().unlockIntent(intentId)