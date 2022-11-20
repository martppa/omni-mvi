package net.asere.omni.mvi

interface QueueContainerHost<State, Effect, Action>
    : StateContainerHost<State, Effect, Action> {
    override val container: Container<State, Effect, Action>
}