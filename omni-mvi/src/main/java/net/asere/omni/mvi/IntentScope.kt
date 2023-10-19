package net.asere.omni.mvi

open class IntentScope(
    open val container: Container,
    internal var errorBlock: (Throwable) -> Unit = {}
)

@StateHostDsl
fun IntentScope.onError(
    block:  (Throwable) -> Unit
) { errorBlock = block }
