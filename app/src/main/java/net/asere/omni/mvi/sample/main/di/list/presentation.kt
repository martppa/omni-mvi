package net.asere.omni.mvi.sample.main.di.list

import net.asere.omni.mvi.sample.list.presentation.ListViewModel
import net.asere.omni.mvi.sample.list.presentation.exception.ExceptionHandler
import net.asere.omni.mvi.sample.list.presentation.exception.LogExceptionHandler
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val listPresentationModule = module {
    factory<ExceptionHandler> { LogExceptionHandler() }
    viewModel { ListViewModel(get(), get(), get()) }
}