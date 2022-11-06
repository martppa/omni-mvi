package com.madapp.omni.mvi

internal fun <UiState, SideEffect, UiAction>
        Container<UiState, SideEffect, UiAction>.asTaskOverrideContainer() =
    seek<TaskOverrideContainer<UiState, SideEffect, UiAction>> {
        it is TaskOverrideContainer<*, *, *>
    }