package net.asere.omni.mvi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import net.asere.omni.core.ExecutableContainer

/**
 * State container
 */
interface StateContainer<State, Effect> : ExposedStateContainer<State, Effect> {
    val state: StateFlow<State>
    val effect: Flow<Effect>
    fun update(function: State.() -> State)
    fun post(effect: Effect)
}

/**
 * Returns itself as an executable container
 *
 * @throws IllegalStateException in case it's not an executable container
 */
fun StateContainer<*, *>.asExecutableContainer(): ExecutableContainer {
    if (this is StateContainerDecorator<*, *>) return seek { it is ExecutableContainer }
    if (this is ExecutableContainer) return this
    throw IllegalStateException("The container is not an Executable container!")
}