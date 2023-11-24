package net.asere.omni.mvi

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import net.asere.omni.core.EmptyCoroutineExceptionHandler
import net.asere.omni.core.ExecutableContainer
import kotlin.coroutines.EmptyCoroutineContext

private const val HANDLE_KEY = "omni_state"

/**
 * This container behaves just like a normal StateContainer but it will
 * store states to the SavedStateHandle
 *
 * @param initialState Initial state to start the container
 * @param savedStateHandle SavedStateHandle used to store the state
 * @param coroutineScope Coroutine scope in which intents will be executed
 * @param coroutineExceptionHandler Handler to deal with exceptions
 */
class SaveableStateContainer<State, Effect> internal constructor(
    override val initialState: State,
    private val savedStateHandle: SavedStateHandle,
    override val coroutineScope: CoroutineScope,
    override val coroutineExceptionHandler: CoroutineExceptionHandler,
) : ExecutableContainer(
    coroutineScope = coroutineScope,
    coroutineExceptionHandler = coroutineExceptionHandler
), StateContainer<State, Effect> {

    override val state = savedStateHandle.getStateFlow(HANDLE_KEY, initialState)

    private val _effect = Channel<Effect>(capacity = Channel.UNLIMITED)
    override val effect = _effect.receiveAsFlow()

    override fun update(function: State.() -> State) {
        val currentState: State = savedStateHandle[HANDLE_KEY] ?: initialState
        savedStateHandle[HANDLE_KEY] = currentState.function()
    }

    override fun post(effect: Effect) {
        _effect.trySend(effect)
    }
}

/**
 * Use this top level extension to build a SaveableStateContainer
 *
 * @param initialState Initial state to start the container
 * @param savedStateHandle SavedStateHandle used to store the state
 * @param coroutineScope Coroutine scope in which intents will be executed, defaulted to CoroutineScope(EmptyCoroutineContext)
 * @param coroutineExceptionHandler Handler to deal with exceptions, defaulted to EmptyCoroutineExceptionHandler
 */
fun <State, Effect>
        StateContainerHost<State, Effect>.saveableStateContainer(
    initialState: State,
    savedStateHandle: SavedStateHandle,
    coroutineScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext),
    coroutineExceptionHandler: CoroutineExceptionHandler = EmptyCoroutineExceptionHandler
) = SaveableStateContainer<State, Effect>(
    initialState = initialState,
    savedStateHandle = savedStateHandle,
    coroutineScope = coroutineScope,
    coroutineExceptionHandler = coroutineExceptionHandler
).decorate { DelegatorContainer(it) }