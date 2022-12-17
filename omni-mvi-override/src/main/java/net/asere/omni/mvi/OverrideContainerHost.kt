package net.asere.omni.mvi

interface OverrideContainerHost<State, Effect>
    : StateContainerHost<State, Effect> {
    override val container: Container<State, Effect>
}