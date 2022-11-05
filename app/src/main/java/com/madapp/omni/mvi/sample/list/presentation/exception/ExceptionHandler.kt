package com.madapp.omni.mvi.sample.list.presentation.exception

interface ExceptionHandler {
    fun handle(throwable: Throwable)
}