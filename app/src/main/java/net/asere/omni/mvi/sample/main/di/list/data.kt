package net.asere.omni.mvi.sample.main.di.list

import net.asere.omni.mvi.sample.list.data.RepoListDataSource
import net.asere.omni.mvi.sample.list.data.RepoListRepositoryImpl
import net.asere.omni.mvi.sample.list.data.network.retrofit.NetworkRepoListDataSource
import net.asere.omni.mvi.sample.list.data.network.retrofit.NetworkServiceBuilder
import net.asere.omni.mvi.sample.list.data.network.retrofit.RepoService
import net.asere.omni.mvi.sample.list.domain.RepoListRepository
import org.koin.dsl.module

val listDataModule = module {
    single<RepoListRepository> { RepoListRepositoryImpl(get()) }
    single<RepoListDataSource> { NetworkRepoListDataSource(get()) }
    single { NetworkServiceBuilder() }
    single { get<NetworkServiceBuilder>().create(RepoService::class.java) }
}