package net.asere.omni.mvi

interface LockContainerHost<State, Effect>
    : StateContainerHost<State, Effect> {
    override val container: Container<State, Effect>
}