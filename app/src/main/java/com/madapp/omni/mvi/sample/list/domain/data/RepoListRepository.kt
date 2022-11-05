package com.madapp.omni.mvi.sample.list.domain.data

import com.madapp.omni.mvi.sample.list.domain.model.PagedRepos

interface RepoListRepository {
    suspend fun getRepositories(page: Int): PagedRepos
}