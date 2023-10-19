package net.asere.omni.mvi

class StateIntentScope<State, Effect>(
    override val container: StateContainer<State, Effect>,
    errorBlock: (Throwable) -> Unit = {}
) : IntentScope(container, errorBlock)

@StateHostDsl
fun <Effect> StateIntentScope<*, Effect>.onError(
    block:  (Throwable) -> Unit
) { errorBlock = block }

@StateHostDsl
fun <State> StateIntentScope<State, *>.postState(
    function: State.() -> State
) = container.update(function)

@StateHostDsl
fun <Effect> StateIntentScope<*, Effect>.postEffect(
    effect:  Effect
) = container.post(effect)
