package com.madapp.omni.mvi

fun <UiState, SideEffect, UiAction> Container<UiState, SideEffect, UiAction>
        .asStateContainer() = this as StateContainer