package net.asere.omni.mvi.sample.list.data

import net.asere.omni.mvi.sample.list.domain.data.RepoListRepository

class RepoListRepositoryImpl(
    private val dataSource: RepoListDataSource,
) : RepoListRepository {

    override suspend fun getRepositories(page: Int) = dataSource.getRepositories(page)
    override suspend fun searchRepositories(
        query: String,
        page: Int
    ) = dataSource.searchRepositories(query, page)
}