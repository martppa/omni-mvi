package net.asere.omni.mvi

@StateHostDsl
fun <State, Effect, Action>
        LockContainerHost<State, Effect, Action>.lockIntent(
    intentId: Any = Unit,
    block: suspend IntentScope<State, Effect>.() -> Unit
) = container.asLockContainer().lockIntent(intentId, block)

fun <State, Effect, Action>
        LockContainerHost<State, Effect, Action>.lockIntent(
    intentId: Any = Unit
) = container.asLockContainer().lockIntent(intentId)