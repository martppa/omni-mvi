package net.asere.omni.mvi

val only = Unit
val units = Unit

open class Take(
    val count: Int
)

infix fun Take.effect(unused: Unit) = TakeEffects(count)
infix fun Take.state(unused: Unit) = TakeStates(count)

open class TakeEffects(count: Int = 0) : Take(count)
open class TakeStates(count: Int = 0) : Take(count)

infix fun Unit.take(count: Int) = Take(count)