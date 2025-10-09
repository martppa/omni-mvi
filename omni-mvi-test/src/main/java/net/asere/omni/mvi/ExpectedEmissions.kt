package net.asere.omni.mvi

sealed interface ExecutionPolicy

sealed interface RunUntil : ExecutionPolicy {
    class StatesEmitted(val count: Int) : RunUntil
    class EffectsEmitted(val count: Int) : RunUntil
    class TotalEmitted(val count: Int) : RunUntil
}

data object Unlimited : ExecutionPolicy
data object DoNotAwait : ExecutionPolicy
