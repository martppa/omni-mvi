package net.asere.omni.mvi

@StateHostDsl
fun <State, Effect, Action>
        OverrideContainerHost<State, Effect, Action>.overrideIntent(
    intentId: Any = Unit,
    block: suspend IntentScope<State, Effect>.() -> Unit
) = container.asOverrideContainer().overrideIntent(intentId, block)