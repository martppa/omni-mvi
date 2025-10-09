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
 * Actual StateContainer implementation
 *
 * @param initialState Starting state of the container
 * @param coroutineScope Execution coroutine scope
 * @param coroutineExceptionHandler Execution handler intended to catch thrown exceptions
 * during execution
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
 * StateContainer builder. Use this function to create it.
 *
 * @param initialState Starting state of the container
 * @param coroutineScope Execution coroutine scope
 * @param coroutineExceptionHandler Execution handler intended to catch thrown exceptions
 * during execution
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

fun <State : Any, Effect : Any> StateContainer<State, Effect>.asStateContainer() =
    this as InnerStateContainer<State, Effect>
