package net.asere.omni.mvi

import net.asere.omni.core.ExecutableContainer

/**
 * Base class for decorating a [StateContainer] with additional functionality.
 *
 * This follows the Decorator pattern, allowing behaviors (like logging, delegation, or
 * threading constraints) to be added to a container dynamically. It implements
 * [InnerStateContainer] by delegating all calls to the wrapped [container].
 *
 * @param State The type of the UI state.
 * @param Effect The type of the side effect.
 * @property container The inner container being decorated.
 */
open class StateContainerDecorator<State : Any, Effect : Any>(
    internal val container: StateContainer<State, Effect>
) : ExecutableContainer(
    coroutineScope = container.coroutineScope,
    coroutineExceptionHandler = container.coroutineExceptionHandler,
),
    InnerStateContainer<State, Effect> {

    override val initialState = container.asStateContainer().initialState
    override val state = container.asStateContainer().state
    override val effect = container.asStateContainer().effect

    override fun update(function: State.() -> State) =
        container.asStateContainer().update(function)

    override fun post(effect: Effect) = container.asStateContainer().post(effect)
}

/**
 * DSL helper for decorating an [InnerStateContainer].
 *
 * @param block A lambda that takes the current container and returns a decorated version.
 * @return The decorated container.
 */
fun<State : Any, Effect : Any> InnerStateContainer<State, Effect>.decorate(
    block: (InnerStateContainer<State, Effect>) -> InnerStateContainer<State, Effect>
): InnerStateContainer<State, Effect> {
    return block(this)
}

/**
 * Recursively searches through a chain of decorators for a container that matches the [predicate].
 *
 * This is useful for finding specific decorator types (e.g., finding the `ExecutableContainer`
 * in a stack of decorators).
 *
 * @param predicate A lambda used to identify the target container.
 * @return The matching container cast to type [T].
 * @throws RuntimeException if no container in the chain matches the predicate.
 */
@Suppress("UNCHECKED_CAST")
fun <T> InnerStateContainer<*, *>.seek(predicate: (Any) -> Boolean): T {
    if (this is StateContainerDecorator) {
        if (predicate(this)) return this as T
        return this.container.asStateContainer().seek(predicate)
    } else if (predicate(this)) {
        return this as T
    } else throw RuntimeException("Container decorator fails. Have you wrapped all containers?")
}
