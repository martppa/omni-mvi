package com.madapp.omni.mvi.sample.list.domain.model

import com.madapp.omni.mvi.sample.shared.domain.model.Repo

data class PagedRepos(
    val currentPage: Int,
    val items: List<Repo>
)