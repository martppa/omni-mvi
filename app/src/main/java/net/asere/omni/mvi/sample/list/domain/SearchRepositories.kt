package net.asere.omni.mvi.sample.list.domain

import net.asere.omni.mvi.sample.list.domain.model.PagedRepos

class SearchRepositories(
    private val repository: RepoListRepository
) {
    suspend operator fun invoke(
        query: String,
        page: Int
    ): PagedRepos = repository.searchRepositories(query, page)
}