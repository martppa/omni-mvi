package net.asere.omni.mvi

import net.asere.omni.core.ExecutionScope
import net.asere.omni.core.OmniHostDsl

class IntentScope<State, Effect>(
    override val container: StateContainer<State, Effect>,
    errorBlock: (Throwable) -> Unit = {}
) : ExecutionScope(container, errorBlock)

@OmniHostDsl
fun <State> IntentScope<State, *>.postState(
    function: State.() -> State
) = container.update(function)

@OmniHostDsl
fun <Effect> IntentScope<*, Effect>.postEffect(
    effect:  Effect
) = container.post(effect)
