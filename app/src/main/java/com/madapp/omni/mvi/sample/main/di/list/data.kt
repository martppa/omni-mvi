package com.madapp.omni.mvi.sample.main.di.list

import com.madapp.omni.mvi.sample.list.data.RepoListDataSource
import com.madapp.omni.mvi.sample.list.data.RepoListRepositoryImpl
import com.madapp.omni.mvi.sample.list.data.network.retrofit.NetworkRepoListDataSource
import com.madapp.omni.mvi.sample.list.data.network.retrofit.NetworkServiceBuilder
import com.madapp.omni.mvi.sample.list.data.network.retrofit.RepoService
import com.madapp.omni.mvi.sample.list.domain.data.RepoListRepository
import org.koin.dsl.module

val listDataModule = module {
    single<RepoListRepository> { RepoListRepositoryImpl(get()) }
    single<RepoListDataSource> { NetworkRepoListDataSource(get()) }
    single { NetworkServiceBuilder() }
    single { get<NetworkServiceBuilder>().create(RepoService::class.java) }
}