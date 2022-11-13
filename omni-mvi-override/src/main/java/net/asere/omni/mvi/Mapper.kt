package net.asere.omni.mvi

internal fun <UiState, SideEffect, UiAction>
        Container<UiState, SideEffect, UiAction>.asOverrideContainer() =
    seek<OverrideContainer<UiState, SideEffect, UiAction>> {
        it is OverrideContainer<*, *, *>
    }