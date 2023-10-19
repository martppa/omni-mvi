package net.asere.omni.mvi

import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

open class StateEmitterContainer<State, Effect> internal constructor(
    initialState: State,
    override val coroutineScope: CoroutineScope,
    override val coroutineExceptionHandler: CoroutineExceptionHandler,
) : ExecutableContainer(
    coroutineScope = coroutineScope,
    coroutineExceptionHandler = coroutineExceptionHandler
), StateContainer<State, Effect> {
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

fun <State, Effect>
        StateContainerHost<State, Effect>.stateContainer(
    initialState: State,
    coroutineScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext),
    coroutineExceptionHandler: CoroutineExceptionHandler = EmptyCoroutineExceptionHandler
) = StateEmitterContainer<State, Effect>(
    initialState,
    coroutineScope,
    coroutineExceptionHandler
).decorate { DelegatorContainer(it) }


fun <State, Effect> ExposedStateContainer<State, Effect>.asStateContainer() =
    this as StateContainer<State, Effect>