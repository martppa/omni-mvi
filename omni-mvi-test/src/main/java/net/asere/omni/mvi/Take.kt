package net.asere.omni.mvi

open class Take(
    val count: Int
)

val take = Unit

sealed class TakeType

data object state : TakeType()
data object effect : TakeType()

infix fun Int.times(type: TakeType): Take = when (type) {
    state -> TakeStates(this)
    effect -> TakeEffects(this)
}

infix fun Int.time(type: TakeType) = times(type)

infix fun Unit.exactly(count: Int) = count

open class TakeEffects(count: Int = 0) : Take(count)
open class TakeStates(count: Int = 0) : Take(count)