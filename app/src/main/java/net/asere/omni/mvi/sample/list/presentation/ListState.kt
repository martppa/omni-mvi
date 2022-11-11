package net.asere.omni.mvi.sample.list.presentation

import net.asere.omni.mvi.sample.shared.domain.extension.empty
import net.asere.omni.mvi.sample.shared.domain.model.Repo

data class ListState(
    val query: String? = null,
    val currentPage: Int = 1,
    val loading: Boolean = false,
    val items: List<Repo> = listOf(),
    val error: String = String.empty()
)

sealed class ListAction {
    object Retry : ListAction()
    object NextPage : ListAction()
    class Query(val value: String) : ListAction()
}

sealed class ListEffect {
    class ShowMessage(val text: String) : ListEffect()
}