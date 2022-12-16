package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope

open class ContainerDecorator<State, Effect, Action>(
    internal val container: Container<State, Effect, Action>
) : StateContainer<State, Effect, Action> {

    override val onAction: (Action) -> Unit = container.onAction
    override val coroutineScope: CoroutineScope = container.coroutineScope
    override val coroutineExceptionHandler: CoroutineExceptionHandler =
        container.coroutineExceptionHandler
    override val state = container.asStateContainer().state
    override val effect = container.asStateContainer().effect
    override fun update(function: State.() -> State) =
        container.asStateContainer().update(function)

    override fun post(effect: Effect) = container.asStateContainer().post(effect)
}

fun<State, Effect, Action> Container<State, Effect, Action>.decorate(
    block: (Container<State, Effect, Action>) -> Container<State, Effect, Action>
): Container<State, Effect, Action> {
    return block(this)
}

@Suppress("UNCHECKED_CAST")
fun <T> Container<*, *, *>.seek(predicate: (Any) -> Boolean): T {
    if (this is ContainerDecorator) {
        if (predicate(this)) return this as T
        return this.container.seek(predicate)
    } else if (predicate(this)) {
        return this as T
    } else throw RuntimeException("Container decorator fails. Have you wrapped all containers?")
}