package net.asere.omni.mvi

interface QueueContainerHost<State, Effect>
    : StateContainerHost<State, Effect> {
    override val container: Container<State, Effect>
}