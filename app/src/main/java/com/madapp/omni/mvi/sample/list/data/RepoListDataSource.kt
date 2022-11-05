package com.madapp.omni.mvi.sample.list.data

import com.madapp.omni.mvi.sample.list.domain.model.PagedRepos

interface RepoListDataSource {
    suspend fun getRepositories(page: Int): PagedRepos
}