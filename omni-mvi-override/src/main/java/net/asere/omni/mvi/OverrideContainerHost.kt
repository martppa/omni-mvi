package net.asere.omni.mvi

import net.asere.omni.core.OmniHostDsl

interface OverrideContainerHost<State, Effect>
    : StateContainerHost<State, Effect> {
    override val container: ExposedStateContainer<State, Effect>
}

@OmniHostDsl
fun <State, Effect>
        OverrideContainerHost<State, Effect>.overrideIntent(
    intentId: Any = Unit,
    block: suspend IntentScope<State, Effect>.() -> Unit
) = container.asOverrideContainer().overrideIntent(intentId, block)