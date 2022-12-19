package net.asere.omni.mvi

interface OverrideContainerHost<State, Effect>
    : StateContainerHost<State, Effect> {
    override val container: Container<State, Effect>
}

@StateHostDsl
fun <State, Effect>
        OverrideContainerHost<State, Effect>.overrideIntent(
    intentId: Any = Unit,
    block: suspend IntentScope<State, Effect>.() -> Unit
) = container.asOverrideContainer().overrideIntent(intentId, block)