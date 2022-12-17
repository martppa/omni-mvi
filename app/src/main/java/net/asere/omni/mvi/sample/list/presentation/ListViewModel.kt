package net.asere.omni.mvi.sample.list.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import net.asere.omni.mvi.ActionContainerHost
import net.asere.omni.mvi.LockContainerHost
import net.asere.omni.mvi.OverrideContainerHost
import net.asere.omni.mvi.currentState
import net.asere.omni.mvi.decorate
import net.asere.omni.mvi.intent
import net.asere.omni.mvi.lockContainer
import net.asere.omni.mvi.lockIntent
import net.asere.omni.mvi.onAction
import net.asere.omni.mvi.onError
import net.asere.omni.mvi.overrideIntent
import net.asere.omni.mvi.postEffect
import net.asere.omni.mvi.postState
import net.asere.omni.mvi.sample.list.domain.usecase.GetRepositories
import net.asere.omni.mvi.sample.list.domain.usecase.SearchRepositories
import net.asere.omni.mvi.sample.list.presentation.exception.ExceptionHandler
import net.asere.omni.mvi.sample.list.presentation.exception.coroutineExceptionHandler
import net.asere.omni.mvi.sample.shared.core.extension.requireMessage
import net.asere.omni.mvi.sample.shared.domain.extension.empty
import net.asere.omni.mvi.stateContainer
import net.asere.omni.mvi.overrideContainer
import net.asere.omni.mvi.unlockIntent

class ListViewModel(
    private val getRepositories: GetRepositories,
    private val searchRepositories: SearchRepositories,
    exceptionHandler: ExceptionHandler
) : ViewModel(),
    ActionContainerHost<ListState, ListEffect, ListAction>,
    LockContainerHost<ListState, ListEffect>,
    OverrideContainerHost<ListState, ListEffect> {

    companion object {
        private const val QUERY_DELAY = 300L
    }

    override val container = stateContainer(
        initialState = ListState(),
        coroutineScope = viewModelScope,
        coroutineExceptionHandler = coroutineExceptionHandler(exceptionHandler)
    ).decorate { lockContainer(it) }
        .decorate { overrideContainer(it) }
        .onAction(::onAction)

    init {
        fetchContent()
    }

    private fun onAction(action: ListAction) {
        when (action) {
            ListAction.NextPage -> nextPage()
            ListAction.Retry -> retry()
            is ListAction.Query -> onQuery(action.value)
        }
    }

    // We are using overrideIntent since we want any ongoing execution to be overridden
    private fun onQuery(value: String) = overrideIntent {
        postState { copy(query = value, currentPage = 1) }
        delay(QUERY_DELAY) // Apply a delay to the intent to reduce query rate
        onError { showError(it) } // Executed when an error occurs
        postState { copy(loading = true) }
        val repos = searchRepositories(query = value, currentState.currentPage)
        postState { copy(loading = false, items = repos.items) }
    }

    // We are using lockIntent since we want this intent to execute only once at a time
    private fun fetchContent() = lockIntent {
        onError { showError(it) } // This block is called when the intent execution fails
        postState { copy(loading = items.isEmpty(), error = String.empty()) }
        val repos = getRepositories(currentState.currentPage)
        postState {
            copy(loading = false, currentPage = repos.currentPage, items = items + repos.items)
        }
        if (repos.items.isEmpty()) {
            lockIntent() // Lock this intent as the end of the list has been reached
        }
    }

    private fun showError(throwable: Throwable) = intent {
        postState { copy(loading = false, error = throwable.requireMessage()) }
        postEffect(ListEffect.ShowMessage(throwable.requireMessage()))
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