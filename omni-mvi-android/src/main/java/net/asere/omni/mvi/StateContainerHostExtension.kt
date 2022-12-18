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

@Composable
fun <Effect> StateContainerHost<*, Effect>.OnEffect(
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

@Composable
fun <UiState> StateContainerHost<UiState, *>.state(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
): State<UiState> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleStateFlow = remember(this, lifecycleOwner) {
        container.asStateContainer().state.flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState)
    }
    return lifecycleStateFlow.collectAsState(currentState)
}

fun <UiState> StateContainerHost<UiState, *>.observeState(
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

fun <Effect> StateContainerHost<*, Effect>.observeEffect(
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