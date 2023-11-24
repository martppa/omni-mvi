package net.asere.omni.mvi.sample.list.domain

import net.asere.omni.mvi.sample.list.domain.model.PagedRepos

class GetRepositories(
    private val repository: RepoListRepository
) {
    suspend operator fun invoke(page: Int): PagedRepos = repository.getRepositories(page)
}