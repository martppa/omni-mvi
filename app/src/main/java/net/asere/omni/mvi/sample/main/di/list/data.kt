package net.asere.omni.mvi.sample.main.di.list

import net.asere.omni.mvi.sample.list.data.network.retrofit.NetworkRepoListDataSource
import net.asere.omni.mvi.sample.list.data.network.retrofit.NetworkServiceBuilder
import net.asere.omni.mvi.sample.list.data.network.retrofit.RepoService
import net.asere.omni.mvi.sample.list.domain.data.RepoListRepository
import org.koin.dsl.module

val listDataModule = module {
    single<RepoListRepository> { net.asere.omni.mvi.sample.list.data.RepoListRepositoryImpl(get()) }
    single<net.asere.omni.mvi.sample.list.data.RepoListDataSource> { NetworkRepoListDataSource(get()) }
    single { NetworkServiceBuilder() }
    single { get<NetworkServiceBuilder>().create(RepoService::class.java) }
}