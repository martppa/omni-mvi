package net.asere.omni.mvi

open class ExecutionScope(
    open val container: Container,
    internal var errorBlock: (Throwable) -> Unit = {}
)

@StateHostDsl
fun ExecutionScope.onError(
    block:  (Throwable) -> Unit
) { errorBlock = block }
