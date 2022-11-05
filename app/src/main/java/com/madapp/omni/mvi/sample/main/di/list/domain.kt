package com.madapp.omni.mvi.sample.main.di.list

import com.madapp.omni.mvi.sample.list.domain.usecase.GetRepositories
import org.koin.dsl.module

val listDomainModule = module {
    single { GetRepositories(get()) }
}