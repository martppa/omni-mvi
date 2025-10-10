package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.CoroutineContext

class AssertableTestCoroutineExceptionHandler(
    private val assertBlock: (Throwable) -> Boolean
) : TestCoroutineExceptionHandler() {

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        if (!assertBlock(exception)) {
            super.handleException(context, exception)
        }
    }
}

open class TestCoroutineExceptionHandler(
    private val coroutineExceptionHandler: CoroutineExceptionHandler = CoroutineExceptionHandler { _, _ -> }
) : CoroutineExceptionHandler {
    private val errorCallBacks: MutableList<(Throwable) -> Unit> = mutableListOf()

    override val key: CoroutineContext.Key<*> get() = coroutineExceptionHandler.key

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        coroutineExceptionHandler.handleException(context, exception)
        errorCallBacks.forEach { it.invoke(exception) }
    }

    internal fun onError(block: (Throwable) -> Unit) {
        errorCallBacks.add(block)
    }
}

fun CoroutineExceptionHandler.onError(block: (Throwable) -> Unit) {
    if (this is TestCoroutineExceptionHandler) {
        onError(block)
    } else {
        throw IllegalArgumentException("Provided CoroutineExceptionHandler is not a TestCoroutineExceptionHandler, " +
                "please provide a TestCoroutineExceptionHandler when building the viewModel")
    }
}
