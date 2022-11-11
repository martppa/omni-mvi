package net.asere.omni.mvi.sample.list.data

import net.asere.omni.mvi.sample.list.domain.model.PagedRepos

interface RepoListDataSource {
    suspend fun getRepositories(page: Int): PagedRepos
    suspend fun searchRepositories(query: String, page: Int): PagedRepos
}
