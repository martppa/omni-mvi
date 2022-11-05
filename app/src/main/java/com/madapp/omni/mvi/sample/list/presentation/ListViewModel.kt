package com.madapp.omni.mvi.sample.list.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madapp.omni.mvi.StateContainerHost
import com.madapp.omni.mvi.LockContainerHost
import com.madapp.omni.mvi.currentState
import com.madapp.omni.mvi.decorate
import com.madapp.omni.mvi.intent
import com.madapp.omni.mvi.lockContainer
import com.madapp.omni.mvi.lockIntent
import com.madapp.omni.mvi.onError
import com.madapp.omni.mvi.postEffect
import com.madapp.omni.mvi.postState
import com.madapp.omni.mvi.sample.list.domain.usecase.GetRepositories
import com.madapp.omni.mvi.sample.shared.core.extension.requireMessage
import com.madapp.omni.mvi.sample.shared.domain.extension.empty
import com.madapp.omni.mvi.sample.list.presentation.exception.ExceptionHandler
import com.madapp.omni.mvi.sample.list.presentation.exception.coroutineExceptionHandler
import com.madapp.omni.mvi.stateContainer
import com.madapp.omni.mvi.unlockIntent

class ListViewModel(
    private val getRepositories: GetRepositories,
    exceptionHandler: ExceptionHandler
) : ViewModel(),
    StateContainerHost<ListState, ListEffect, ListAction>,
    LockContainerHost<ListState, ListEffect, ListAction> {

    override val container = stateContainer(
        initialState = ListState(),
        onAction = ::onAction,
        coroutineScope = viewModelScope,
        coroutineExceptionHandler = coroutineExceptionHandler(exceptionHandler)
    ).decorate { lockContainer(it) }

    init {
        fetchContent()
    }

    private fun onAction(action: ListAction) {
        when (action) {
            ListAction.NextPage -> nextPage()
            ListAction.Retry -> retry()
        }
    }

    // We are using lockIntent since we want this intent to execute only once at a time
    private fun fetchContent() = lockIntent {
        onError { // This block is called when the intent execution fails
            postState { copy(loading = false, error = it.requireMessage()) }
            postEffect(ListEffect.ShowMessage(it.requireMessage()))
        }
        postState { copy(loading = items.isEmpty(), error = String.empty()) }
        val repos = getRepositories(currentState.currentPage)
        postState {
            copy(loading = false, currentPage = repos.currentPage, items = items + repos.items)
        }
        if (repos.items.isEmpty()) {
            lockIntent() // Lock this intent as the end of the list has been reached
        }
    }

    private fun retry() = intent {
        unlockIntent() // Unlock intent to allow content fetch
        fetchContent()
    }

    private fun nextPage() = intent {
        postState { copy(currentPage = currentPage + 1) }
        fetchContent()
    }
}