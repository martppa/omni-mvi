package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import net.asere.omni.core.EmptyCoroutineExceptionHandler
import net.asere.omni.core.ExecutableContainer
import kotlin.coroutines.EmptyCoroutineContext

/**
 * The default concrete implementation of [InnerStateContainer].
 *
 * This class manages the actual [MutableStateFlow] for state and a [Channel] for side effects.
 * It is marked `internal` to encourage using the [stateContainer] factory function.
 *
 * @param initialState The starting state.
 * @param coroutineScope The scope for internal operations.
 * @param coroutineExceptionHandler Handler for unexpected errors.
 */
class StateContainerConcretion<State : Any, Effect : Any> internal constructor(
    override val initialState: State,
    override val coroutineScope: CoroutineScope,
    override val coroutineExceptionHandler: CoroutineExceptionHandler,
) : ExecutableContainer(
    coroutineScope = coroutineScope,
    coroutineExceptionHandler = coroutineExceptionHandler
),
    InnerStateContainer<State, Effect> {
    private val _state: MutableStateFlow<State> = MutableStateFlow(initialState)
    override val state = _state.asStateFlow()

    private val _effect = Channel<Effect>(capacity = Channel.UNLIMITED)
    override val effect = _effect.receiveAsFlow()

    override fun update(function: State.() -> State) {
        _state.update { it.function() }
    }

    override fun post(effect: Effect) {
        _effect.trySend(effect)
    }
}

/**
 * Creates and configures a new [StateContainer] for a [StateContainerHost].
 *
 * This is the standard way to initialize a container in a ViewModel or other host.
 * It automatically wraps the container in a [DelegatorContainer] to support delegation.
 *
 * @param initialState The starting state of the container.
 * @param coroutineScope The scope where intents will run. Defaults to a new scope.
 * @param coroutineExceptionHandler Handler for errors. Defaults to [EmptyCoroutineExceptionHandler].
 * @return A fully configured [InnerStateContainer].
 */
fun <State : Any, Effect : Any>
        StateContainerHost<State, Effect>.stateContainer(
        initialState: State,
        coroutineScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext),
        coroutineExceptionHandler: CoroutineExceptionHandler = EmptyCoroutineExceptionHandler
    ) = StateContainerConcretion<State, Effect>(
    initialState = initialState,
    coroutineScope = coroutineScope,
    coroutineExceptionHandler = coroutineExceptionHandler
).decorate { DelegatorContainer(it) }

/**
 * Extension to safely cast a [StateContainer] to its internal [InnerStateContainer] representation.
 */
fun <State : Any, Effect : Any> StateContainer<State, Effect>.asStateContainer() =
    this as InnerStateContainer<State, Effect>
