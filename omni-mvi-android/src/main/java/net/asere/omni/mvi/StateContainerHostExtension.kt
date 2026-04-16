package net.asere.omni.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

/**
 * A Composable function to observe and handle side effects from a [StateContainerHost].
 *
 * This function uses [LaunchedEffect] to collect effects from the container while the
 * lifecycle is at least in the [minActiveState].
 *
 * @param minActiveState The minimum lifecycle state required to collect effects. Defaults to [Lifecycle.State.STARTED].
 * @param action A [FlowCollector] (usually a lambda) that will be invoked for each emitted effect.
 */
@Composable
fun <UiState : Any, Effect : Any> StateContainerHost<UiState, Effect>.OnEffect(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: FlowCollector<Effect>
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(this, lifecycleOwner) {
        launch {
            container.asStateContainer().effect.flowWithLifecycle(
                lifecycleOwner.lifecycle,
                minActiveState
            ).collect(action)
        }
    }
}

/**
 * Returns the current state of the [StateContainerHost] as a Compose [State].
 *
 * The state is collected in a lifecycle-aware manner, ensuring that updates are only
 * processed when the lifecycle is at least in the [minActiveState].
 *
 * @param minActiveState The minimum lifecycle state required to collect state updates. Defaults to [Lifecycle.State.STARTED].
 * @return A Compose [State] reflecting the current UI state.
 */
@Composable
fun <UiState : Any> StateContainerHost<UiState, *>.state(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
): State<UiState> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleStateFlow = remember(this, lifecycleOwner) {
        container.asStateContainer().state.flowWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
             minActiveState = minActiveState
        )
    }
    return lifecycleStateFlow.collectAsState(currentState)
}

/**
 * Sets up a lifecycle-aware observer for state changes.
 *
 * This is useful for observing state from outside of Compose (e.g., in a Fragment or Activity).
 * The [onState] block is executed within the [repeatOnLifecycle] block.
 *
 * @param lifecycleOwner The owner of the lifecycle to observe.
 * @param lifecycleState The minimum state required for observation. Defaults to [Lifecycle.State.STARTED].
 * @param onState The callback invoked when a new state is emitted.
 */
fun <UiState : Any> StateContainerHost<UiState, *>.observeState(
    lifecycleOwner: LifecycleOwner,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    onState: (suspend (state: UiState) -> Unit)
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.lifecycle.repeatOnLifecycle(lifecycleState) {
            launch { container.asStateContainer().state.collect { onState(it) } }
        }
    }
}

/**
 * Sets up a lifecycle-aware observer for side effects.
 *
 * This is useful for observing effects from outside of Compose (e.g., in a Fragment or Activity).
 * The [onEffect] block is executed within the [repeatOnLifecycle] block.
 *
 * @param lifecycleOwner The owner of the lifecycle to observe.
 * @param lifecycleState The minimum state required for observation. Defaults to [Lifecycle.State.STARTED].
 * @param onEffect The callback invoked when a new effect is emitted.
 */
fun <Effect : Any> StateContainerHost<*, Effect>.observeEffect(
    lifecycleOwner: LifecycleOwner,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    onEffect: (suspend (effect: Effect) -> Unit)
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.lifecycle.repeatOnLifecycle(lifecycleState) {
            launch { container.asStateContainer().effect.collect { onEffect(it) } }
        }
    }
}
