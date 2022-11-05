package com.madapp.omni.mvi.sample.list.presentation.exception

import android.util.Log
import com.madapp.omni.mvi.sample.shared.core.extension.requireMessage

class LogExceptionHandler : ExceptionHandler {
    override fun handle(throwable: Throwable) {
        Log.e("Log handler", throwable.requireMessage())
    }
}