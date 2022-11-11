package net.asere.omni.mvi.sample.main.di.list

import net.asere.omni.mvi.sample.list.domain.usecase.GetRepositories
import net.asere.omni.mvi.sample.list.domain.usecase.SearchRepositories
import org.koin.dsl.module

val listDomainModule = module {
    single { GetRepositories(get()) }
    single { SearchRepositories(get()) }
}