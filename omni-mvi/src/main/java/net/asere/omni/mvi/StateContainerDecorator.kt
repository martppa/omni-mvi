package net.asere.omni.mvi

import net.asere.omni.core.ExecutableContainer

/**
 * Basic container decorator implementation. Its function is to decorate any
 * container with its own features.
 */
open class StateContainerDecorator<State, Effect>(
    internal val container: StateContainer<State, Effect>
) : ExecutableContainer(
    coroutineScope = container.coroutineScope,
    coroutineExceptionHandler = container.coroutineExceptionHandler,
), InnerStateContainer<State, Effect> {

    override val initialState = container.asStateContainer().initialState
    override val state = container.asStateContainer().state
    override val effect = container.asStateContainer().effect
    override fun update(function: State.() -> State) =
        container.asStateContainer().update(function)

    override fun post(effect: Effect) = container.asStateContainer().post(effect)
}

/**
 * Helper function that eases container decoration
 *
 * @param block Block of code where decoration takes place
 * @return Decorated container
 */
fun<State, Effect> InnerStateContainer<State, Effect>.decorate(
    block: (InnerStateContainer<State, Effect>) -> InnerStateContainer<State, Effect>
): InnerStateContainer<State, Effect> {
    return block(this)
}

/**
 * Recursively seeks decorators that match the predicate
 *
 * @param predicate Predicate lambda that performs comparison
 * @return Matching container
 * @throws RuntimeException when no container matches the predicate
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