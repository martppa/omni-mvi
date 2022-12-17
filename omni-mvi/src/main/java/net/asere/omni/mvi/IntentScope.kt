package net.asere.omni.mvi

class IntentScope<State, Effect>(
    val container: StateContainer<State, Effect>,
    internal var errorBlock: (Throwable) -> Unit = {}
)

@StateHostDsl
fun <Effect> IntentScope<*, Effect>.onError(
    block:  (Throwable) -> Unit
) { errorBlock = block }

@StateHostDsl
fun <State> IntentScope<State, *>.postState(
    function: State.() -> State
) = container.update(function)

@StateHostDsl
fun <Effect> IntentScope<*, Effect>.postEffect(
    effect:  Effect
) = container.post(effect)
