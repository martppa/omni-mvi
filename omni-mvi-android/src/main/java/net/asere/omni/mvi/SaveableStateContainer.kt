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
 * A [StateContainer] implementation that automatically persists and restores its state
 * using Android's [SavedStateHandle].
 *
 * This is essential for handling process death in Android. The state is serialized and
 * stored under a specific key in the handle whenever it's updated.
 *
 * @param State The type representing the UI state. Must be parcelable or serializable to work with [SavedStateHandle].
 * @param Effect The type representing side effects.
 * @property initialState The starting state if no state is found in the [savedStateHandle].
 * @property savedStateHandle The Android lifecycle component used for state persistence.
 * @property coroutineScope The scope where intents will be executed.
 * @property coroutineExceptionHandler Handler for uncaught exceptions.
 */
class SaveableStateContainer<State : Any, Effect : Any> internal constructor(
    override val initialState: State,
    private val savedStateHandle: SavedStateHandle,
    override val coroutineScope: CoroutineScope,
    override val coroutineExceptionHandler: CoroutineExceptionHandler,
) : ExecutableContainer(
    coroutineScope = coroutineScope,
    coroutineExceptionHandler = coroutineExceptionHandler
), InnerStateContainer<State, Effect> {

    /**
     * A [kotlinx.coroutines.flow.StateFlow] backed by [SavedStateHandle].
     * It emits the restored state initially, or [initialState] if none exists.
     */
    override val state = savedStateHandle.getStateFlow(HANDLE_KEY, initialState)

    private val _effect = Channel<Effect>(capacity = Channel.UNLIMITED)
    override val effect = _effect.receiveAsFlow()

    /**
     * Updates the state and persists the new value into [SavedStateHandle].
     *
     * @param function A lambda that receives the current state and returns the new state.
     */
    override fun update(function: State.() -> State) {
        val currentState: State = savedStateHandle[HANDLE_KEY] ?: initialState
        savedStateHandle[HANDLE_KEY] = currentState.function()
    }

    override fun post(effect: Effect) {
        _effect.trySend(effect)
    }
}

/**
 * Creates and configures a new [SaveableStateContainer] for a [StateContainerHost].
 *
 * This is the Android-specific equivalent of `stateContainer`, designed to be used
 * in ViewModels that receive a [SavedStateHandle].
 *
 * @param initialState The starting state.
 * @param savedStateHandle The handle used for state restoration.
 * @param coroutineScope The scope for execution. Defaults to a new scope.
 * @param coroutineExceptionHandler Handler for errors. Defaults to [EmptyCoroutineExceptionHandler].
 * @return A fully configured [InnerStateContainer] with persistence capabilities.
 */
fun <State : Any, Effect : Any>
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
