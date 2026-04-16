package net.asere.omni.mvi

import net.asere.omni.core.OmniHostDsl

/**
 * A specialized [StateContainerHost] that supports intents that override previous executions.
 *
 * Implement this interface in your ViewModel or host class to enable the `overrideIntent`
 * DSL function.
 */
interface OverrideContainerHost<State : Any, Effect : Any>
    : StateContainerHost<State, Effect> {
    /**
     * The [StateContainer] managed by this host.
     */
    override val container: StateContainer<State, Effect>
}

/**
 * Starts an intent, canceling and joining any previous execution identified by [intentId].
 *
 * This pattern is useful for actions where only the result of the latest request matters,
 * such as search-as-you-type or refreshing data.
 *
 * @param intentId The identifier used to track the intent. Defaults to [Unit].
 * @param block The suspendable logic to execute.
 */
@OmniHostDsl
fun <State : Any, Effect : Any>
        OverrideContainerHost<State, Effect>.overrideIntent(
    intentId: Any = Unit,
    block: suspend IntentScope<State, Effect>.() -> Unit
) = container.asOverrideContainer().overrideIntent(intentId, block)
