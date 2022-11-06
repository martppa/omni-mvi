package net.asere.omni.mvi.sample.list.data.network.retrofit

import net.asere.omni.mvi.sample.list.data.RepoListDataSource
import net.asere.omni.mvi.sample.list.data.network.toDomain
import net.asere.omni.mvi.sample.list.domain.model.PagedRepos

class NetworkRepoListDataSource(
    private val service: RepoService
) : RepoListDataSource {

    override suspend fun getRepositories(page: Int): PagedRepos {
        val items = service.getRepositories(page).map { it.toDomain() }
        return PagedRepos(page, items)
    }
}