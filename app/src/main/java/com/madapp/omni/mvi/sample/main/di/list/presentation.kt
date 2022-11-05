package com.madapp.omni.mvi.sample.main.di.list

import com.madapp.omni.mvi.sample.list.presentation.ListViewModel
import com.madapp.omni.mvi.sample.list.presentation.exception.ExceptionHandler
import com.madapp.omni.mvi.sample.list.presentation.exception.LogExceptionHandler
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val listPresentationModule = module {
    factory<ExceptionHandler> { LogExceptionHandler() }
    viewModel { ListViewModel(get(), get()) }
}