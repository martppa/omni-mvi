package net.asere.omni.mvi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface StateContainer<State, Effect>
    : Container<State, Effect> {
    val state: StateFlow<State>
    val effect: Flow<Effect>
    fun update(function: State.() -> State)
    fun post(effect: Effect)
}