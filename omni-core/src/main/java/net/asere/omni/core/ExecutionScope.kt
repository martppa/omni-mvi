package net.asere.omni.core

open class ExecutionScope(
    internal var errorBlock: (Throwable) -> Unit = {}
)

@OmniHostDsl
fun ExecutionScope.onError(
    block:  (Throwable) -> Unit
) { errorBlock = block }

fun <Scope : ExecutionScope> ExecutionScope.map(
    scope: Scope,
    block: suspend Scope.() -> Unit
): suspend Scope.() -> Unit  {
    onError { scope.errorBlock(it) }
    return block
}