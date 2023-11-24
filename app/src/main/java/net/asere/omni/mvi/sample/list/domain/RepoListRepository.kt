package net.asere.omni.mvi.sample.list.domain

import net.asere.omni.mvi.sample.list.domain.model.PagedRepos

interface RepoListRepository {
    suspend fun getRepositories(page: Int): PagedRepos
    suspend fun searchRepositories(query: String, page: Int): PagedRepos
}