package net.asere.omni.mvi

open class ExpectedEmissions(
    val count: Int
)

open class EffectsEmitted(count: Int) : ExpectedEmissions(count)
open class StatesEmitted(count: Int) : ExpectedEmissions(count)
open class AnyEmitted(count: Int) : ExpectedEmissions(count)
object DoNotExpect : ExpectedEmissions(0)
object Unlimited : ExpectedEmissions(-1)