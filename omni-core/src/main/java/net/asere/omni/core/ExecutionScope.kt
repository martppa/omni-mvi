package net.asere.omni.core

open class ExecutionScope(
    open val container: Container,
    internal var errorBlock: (Throwable) -> Unit = {}
)

@OmniHostDsl
fun ExecutionScope.onError(
    block:  (Throwable) -> Unit
) { errorBlock = block }
