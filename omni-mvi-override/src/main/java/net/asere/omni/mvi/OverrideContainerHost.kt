package net.asere.omni.mvi

interface OverrideContainerHost<State, Effect>
    : StateContainerHost<State, Effect> {
    override val container: ExposedStateContainer<State, Effect>
}

@StateHostDsl
fun <State, Effect>
        OverrideContainerHost<State, Effect>.overrideIntent(
    intentId: Any = Unit,
    block: suspend StateIntentScope<State, Effect>.() -> Unit
) = container.asOverrideContainer().overrideIntent(intentId, block)