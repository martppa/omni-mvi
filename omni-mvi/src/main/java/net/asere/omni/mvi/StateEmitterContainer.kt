package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _effect = MutableSharedFlow<Effect>()
    override val effect = _effect.asSharedFlow()

    override fun update(function: State.() -> State) {
        _state.update { it.function() }
    }

    override fun post(effect: Effect) {
        _effect.tryEmit(effect)
    }
}

fun <State, Effect> Container<State, Effect>.asStateContainer() =
    this as StateContainer