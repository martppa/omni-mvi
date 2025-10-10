package net.asere.omni.mvi

sealed interface ExecutionPolicy

sealed interface RunMode {
    data object Sequential : RunMode
    data object Async : RunMode
}

sealed class RunUntil(
    internal val mode: RunMode
) : ExecutionPolicy {
    class StatesEmitted(val count: Int, mode: RunMode = RunMode.Sequential) : RunUntil(mode)
    class EffectsEmitted(val count: Int, mode: RunMode = RunMode.Sequential) : RunUntil(mode)
    class TotalEmitted(val count: Int, mode: RunMode = RunMode.Sequential) : RunUntil(mode)
}

data object Unlimited : ExecutionPolicy
data object DoNotAwait : ExecutionPolicy
