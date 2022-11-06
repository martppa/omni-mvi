package net.asere.omni.mvi.sample.list.domain.data

import net.asere.omni.mvi.sample.list.domain.model.PagedRepos

interface RepoListRepository {
    suspend fun getRepositories(page: Int): PagedRepos
}