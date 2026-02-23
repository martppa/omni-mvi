package net.asere.omni.mvi.sample.list.presentation

interface ActionHost<Action> {
    fun on(action: Action)
}