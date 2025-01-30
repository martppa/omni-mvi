package net.asere.omni.mvi

interface ActionContainer<State : Any, Effect : Any, Action : Any> : StateContainer<State, Effect>