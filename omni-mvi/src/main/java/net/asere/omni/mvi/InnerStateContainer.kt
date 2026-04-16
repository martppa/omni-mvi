package net.asere.omni.mvi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import net.asere.omni.core.ExecutableContainer

/**
 * An internal representation of a [StateContainer] that exposes state and effect streams
 * and allows for updates.
 *
 * This interface provides the bridge between the public-facing [StateContainer] and the
 * internal execution logic of the MVI pattern.
 *
 * @param State The type of the UI state.
 * @param Effect The type of the side effect.
 */
interface InnerStateContainer<State : Any, Effect : Any> : StateContainer<State, Effect> {
    /**
     * The initial value of the [State].
     */
    val initialState: State

    /**
     * A [StateFlow] that emits the current [State].
     */
    val state: StateFlow<State>

    /**
     * A [Flow] that emits side [Effect]s.
     */
    val effect: Flow<Effect>

    /**
     * Updates the current [State] using the provided [function].
     *
     * @param function A lambda that receives the current state as its receiver and returns the new state.
     */
    fun update(function: State.() -> State)

    /**
     * Posts a new side [Effect] to the [effect] stream.
     *
     * @param effect The side effect to emit.
     */
    fun post(effect: Effect)
}

/**
 * Attempts to cast or resolve this [InnerStateContainer] into an [ExecutableContainer].
 *
 * This is useful for accessing low-level execution controls (like `await` or `cancel`)
 * from a state container instance.
 *
 * @return The [ExecutableContainer] instance.
 * @throws IllegalStateException if this container is not an [ExecutableContainer] and
 * doesn't decorate one.
 */
fun InnerStateContainer<*, *>.asExecutableContainer(): ExecutableContainer {
    if (this is StateContainerDecorator<*, *>) return seek { it is ExecutableContainer }
    if (this is ExecutableContainer) return this
    throw IllegalStateException("The container is not an Executable container!")
}
