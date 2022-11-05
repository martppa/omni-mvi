package com.madapp.omni.mvi.sample.list.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madapp.omni.mvi.Container
import com.madapp.omni.mvi.StateContainerHost
import com.madapp.omni.mvi.sample.list.domain.model.PagedRepos
import com.madapp.omni.mvi.sample.list.domain.usecase.GetRepositories
import com.madapp.omni.mvi.sample.shared.core.extension.requireMessage
import com.madapp.omni.mvi.sample.shared.domain.extension.empty
import com.madapp.omni.mvi.sample.shared.domain.model.Repo
import com.madapp.omni.mvi.sample.list.presentation.exception.ExceptionHandler
import com.madapp.omni.mvi.sample.list.presentation.exception.coroutineExceptionHandler
import com.madapp.omni.mvi.stateContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ListViewModel(
    private val getRepositories: GetRepositories,
    exceptionHandler: ExceptionHandler
) : ViewModel(), StateContainerHost<ListState, ListEffect, ListAction> {

    override val container = stateContainer(
        initialState = ListState(),
        onAction = ::onAction,
        coroutineScope = viewModelScope,
        coroutineExceptionHandler = coroutineExceptionHandler(exceptionHandler)
    )

    init {
        fetchContent()
    }

    private fun onAction(action: ListAction) {

    }

    private fun fetchContent() = lock
        endReached = false
        mutableState.update { it.copy(loading = true, error = String.empty()) }
        executeTask { getRepositories(currentPage) }
            .onSuccess(::onContentFetched)
            .onFailure(::onFetchError)
        mutableState.update { it.copy(loading = false) }
    }

    private fun onContentFetched(content: PagedRepos) {
        endReached = content.items.isEmpty()
        currentPage = content.currentPage
        fetchedItems.addAll(content.items)
        mutableState.update { it.copy(items = fetchedItems) }
    }

    private fun onFetchError(error: Throwable) {
        mutableState.update { it.copy(error = error.requireMessage()) }
    }

    fun onRetry() {
        fetchContent()
    }

    fun nextPage() {
        if (allowedToNextPage()) {
            currentPage++
            fetchContent()
        }
    }

    private fun allowedToNextPage() =
        !state.value.loading
                && state.value.error.isEmpty()
                && !endReached
}