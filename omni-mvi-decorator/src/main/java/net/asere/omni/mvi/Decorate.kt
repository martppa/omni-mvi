package net.asere.omni.mvi

fun<State, Effect, Action> Container<State, Effect, Action>.decorate(
    block: (
        Container<State, Effect, Action>
    ) -> Container<State, Effect, Action>
): Container<State, Effect, Action> {
    return block(this)
}