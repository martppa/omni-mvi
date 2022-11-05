package com.madapp.omni.mvi.sample.list.presentation

import com.madapp.omni.mvi.sample.shared.domain.extension.empty
import com.madapp.omni.mvi.sample.shared.domain.model.Repo

data class ListState(
    val currentPage: Int = 1,
    val loading: Boolean = false,
    val items: List<Repo> = listOf(),
    val error: String = String.empty()
)

sealed class ListAction {
    object Retry : ListAction()
    object NextPage : ListAction()
}

sealed class ListEffect {
    class ShowMessage(val text: String) : ListEffect()
}