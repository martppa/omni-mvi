package net.asere.omni.mvi

class IntentScope<State, Effect>(
    override val container: StateContainer<State, Effect>,
    errorBlock: (Throwable) -> Unit = {}
) : ExecutionScope(container, errorBlock)

@StateHostDsl
fun <State> IntentScope<State, *>.postState(
    function: State.() -> State
) = container.update(function)

@StateHostDsl
fun <Effect> IntentScope<*, Effect>.postEffect(
    effect:  Effect
) = container.post(effect)
