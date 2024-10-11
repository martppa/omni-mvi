package net.asere.omni.mvi

import net.asere.omni.core.ExecutionScope
import net.asere.omni.core.OmniHostDsl

class IntentScope<State, Effect>(
    val container: InnerStateContainer<State, Effect>,
) : ExecutionScope()

@Deprecated("Use reduce extension function", ReplaceWith("reduce(function)"))
@OmniHostDsl
fun <State> IntentScope<State, *>.postState(
    function: State.() -> State
) = container.update(function)

@OmniHostDsl
fun <State> IntentScope<State, *>.reduce(
    function: State.() -> State
) = container.update(function)

@Deprecated("Use emit extension function", ReplaceWith("post(Effect)"))
@OmniHostDsl
fun <Effect> IntentScope<*, Effect>.postEffect(
    effect:  Effect
) = container.post(effect)

@OmniHostDsl
fun <Effect> IntentScope<*, Effect>.post(
    vararg effects:  Effect
) {
    effects.forEach { container.post(it) }
}
