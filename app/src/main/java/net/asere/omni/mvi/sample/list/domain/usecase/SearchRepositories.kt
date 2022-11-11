package net.asere.omni.mvi.sample.list.domain.usecase

import net.asere.omni.mvi.sample.list.domain.data.RepoListRepository

class SearchRepositories(
    private val repository: RepoListRepository
) {
    suspend operator fun invoke(
        query: String,
        page: Int
    ) = repository.searchRepositories(query, page)
}