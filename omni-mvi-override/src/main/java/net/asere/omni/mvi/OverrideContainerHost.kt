package net.asere.omni.mvi

import net.asere.omni.core.OmniHostDsl

/**
 * Host of Override Containers
 */
interface OverrideContainerHost<State, Effect>
    : StateContainerHost<State, Effect> {
    override val container: ExposedStateContainer<State, Effect>
}

/**
 * Start an intent overriding its previous execution
 *
 * @param intentId Intent identifier
 * @param block Intent's content
 */
@OmniHostDsl
fun <State, Effect>
        OverrideContainerHost<State, Effect>.overrideIntent(
    intentId: Any = Unit,
    block: suspend IntentScope<State, Effect>.() -> Unit
) = container.asOverrideContainer().overrideIntent(intentId, block)