package net.asere.omni.mvi

@StateHostDsl
fun <State, Effect>
        LockContainerHost<State, Effect>.lockIntent(
    intentId: Any = Unit,
    block: suspend IntentScope<State, Effect>.() -> Unit
) = container.asLockContainer().lockIntent(intentId, block)

fun <State, Effect>
        LockContainerHost<State, Effect>.lockIntent(
    intentId: Any = Unit
) = container.asLockContainer().lockIntent(intentId)