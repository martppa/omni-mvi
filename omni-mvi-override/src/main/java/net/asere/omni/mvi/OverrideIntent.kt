package net.asere.omni.mvi

@StateHostDsl
fun <State, Effect>
        OverrideContainerHost<State, Effect>.overrideIntent(
    intentId: Any = Unit,
    block: suspend IntentScope<State, Effect>.() -> Unit
) = container.asOverrideContainer().overrideIntent(intentId, block)