package net.asere.omni.mvi

import net.asere.omni.core.OmniHostDsl

/**
 * Host of Override Containers
 */
interface OverrideContainerHost<State : Any, Effect : Any>
    : StateContainerHost<State, Effect> {
    override val container: StateContainer<State, Effect>
}

/**
 * Start an intent overriding its previous execution
 *
 * @param intentId Intent identifier
 * @param block Intent's content
 */
@OmniHostDsl
fun <State : Any, Effect : Any>
        OverrideContainerHost<State, Effect>.overrideIntent(
    intentId: Any = Unit,
    block: suspend IntentScope<State, Effect>.() -> Unit
) = container.asOverrideContainer().overrideIntent(intentId, block)