package net.asere.omni.mvi

import net.asere.omni.core.ExecutionScope
import net.asere.omni.core.OmniHostDsl

class IntentScope<State : Any, Effect : Any>(
    val container: InnerStateContainer<State, Effect>,
) : ExecutionScope()

@Deprecated("Use reduce extension function", ReplaceWith("reduce(function)"))
@OmniHostDsl
fun <State : Any> IntentScope<State, *>.postState(
    function: State.() -> State
) = container.update(function)

@OmniHostDsl
fun <State : Any> IntentScope<State, *>.reduce(
    function: State.() -> State
) = container.update(function)

@Deprecated("Use emit extension function", ReplaceWith("post(Effect)"))
@OmniHostDsl
fun <Effect : Any> IntentScope<*, Effect>.postEffect(
    effect:  Effect
) = container.post(effect)

@OmniHostDsl
fun <Effect : Any> IntentScope<*, Effect>.post(
    vararg effects:  Effect
) {
    effects.forEach { container.post(it) }
}
