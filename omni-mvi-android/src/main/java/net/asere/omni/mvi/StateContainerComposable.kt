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
fun <SideEffect> StateContainerHost<*, SideEffect, *>.OnEffect(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: FlowCollector<SideEffect>
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(this, lifecycleOwner) {
        launch {
            container.asStateContainer().uiEffect.flowWithLifecycle(
                lifecycleOwner.lifecycle,
                minActiveState
            ).collect(action)
        }
    }
}

@Composable
fun <UiState, SideEffect, UiAction> StateContainerHost<UiState, SideEffect, UiAction>.state(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
): State<UiState> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleStateFlow = remember(this, lifecycleOwner) {
        container.asStateContainer().uiState.flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState)
    }
    return lifecycleStateFlow.collectAsState(currentState)
}