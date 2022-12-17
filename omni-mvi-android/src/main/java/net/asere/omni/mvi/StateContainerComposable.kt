package net.asere.omni.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
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
fun <UiState, Effect> StateContainerHost<UiState, Effect>.state(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
): State<UiState> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleStateFlow = remember(this, lifecycleOwner) {
        container.asStateContainer().state.flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState)
    }
    return lifecycleStateFlow.collectAsState(currentState)
}