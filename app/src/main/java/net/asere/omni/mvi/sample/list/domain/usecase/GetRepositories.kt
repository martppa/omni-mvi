package net.asere.omni.mvi.sample.list.domain.usecase

import net.asere.omni.mvi.sample.list.domain.data.RepoListRepository

class GetRepositories(
    private val repository: RepoListRepository
) {
    suspend operator fun invoke(page: Int) = repository.getRepositories(page)
}