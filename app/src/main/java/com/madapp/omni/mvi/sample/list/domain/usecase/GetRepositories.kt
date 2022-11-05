package com.madapp.omni.mvi.sample.list.domain.usecase

import com.madapp.omni.mvi.sample.list.domain.data.RepoListRepository

class GetRepositories(
    private val repository: RepoListRepository
) {
    suspend operator fun invoke(page: Int) = repository.getRepositories(page)
}