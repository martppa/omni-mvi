package net.asere.omni.mvi.sample.list.domain.model

import net.asere.omni.mvi.sample.shared.domain.model.Repo

data class PagedRepos(
    val currentPage: Int,
    val items: List<Repo>
)