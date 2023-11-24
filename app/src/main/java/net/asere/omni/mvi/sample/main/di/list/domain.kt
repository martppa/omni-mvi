package net.asere.omni.mvi.sample.main.di.list

import net.asere.omni.mvi.sample.list.domain.GetRepositories
import net.asere.omni.mvi.sample.list.domain.SearchRepositories
import org.koin.dsl.module

val listDomainModule = module {
    single { GetRepositories(get()) }
    single { SearchRepositories(get()) }
}