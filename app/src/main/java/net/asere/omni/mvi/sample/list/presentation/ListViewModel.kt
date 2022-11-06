package net.asere.omni.mvi.sample.list.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import net.asere.omni.mvi.StateContainerHost
import net.asere.omni.mvi.LockContainerHost
import net.asere.omni.mvi.currentState
import net.asere.omni.mvi.decorate
import net.asere.omni.mvi.intent
import net.asere.omni.mvi.lockContainer
import net.asere.omni.mvi.lockIntent
import net.asere.omni.mvi.onError
import net.asere.omni.mvi.postEffect
import net.asere.omni.mvi.postState
import net.asere.omni.mvi.sample.list.domain.usecase.GetRepositories
import net.asere.omni.mvi.sample.shared.core.extension.requireMessage
import net.asere.omni.mvi.sample.shared.domain.extension.empty
import net.asere.omni.mvi.sample.list.presentation.exception.ExceptionHandler
import net.asere.omni.mvi.sample.list.presentation.exception.coroutineExceptionHandler
import net.asere.omni.mvi.stateContainer
import net.asere.omni.mvi.unlockIntent

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