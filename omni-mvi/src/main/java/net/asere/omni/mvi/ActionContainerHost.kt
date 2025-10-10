package net.asere.omni.mvi

/**
 * Action container's Host
 */
interface ActionContainerHost<State : Any, Effect : Any, Action : Any>
    : StateContainerHost<State, Effect> {

    fun on(action: Action)
}