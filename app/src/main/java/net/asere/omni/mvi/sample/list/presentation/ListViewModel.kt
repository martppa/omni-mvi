package net.asere.omni.mvi.sample.list.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import net.asere.omni.mvi.ActionContainerHost
import net.asere.omni.mvi.LockContainerHost
import net.asere.omni.mvi.OverrideContainerHost
import net.asere.omni.mvi.buildLockContainer
import net.asere.omni.mvi.buildOverrideContainer
import net.asere.omni.mvi.currentState
import net.asere.omni.mvi.intent
import net.asere.omni.mvi.lockIntent
import net.asere.omni.mvi.onAction
import net.asere.omni.core.onError
import net.asere.omni.mvi.overrideIntent
import net.asere.omni.mvi.post
import net.asere.omni.mvi.postEffect
import net.asere.omni.mvi.postState
import net.asere.omni.mvi.reduce
import net.asere.omni.mvi.sample.list.domain.usecase.GetRepositories
import net.asere.omni.mvi.sample.list.domain.usecase.SearchRepositories
import net.asere.omni.mvi.sample.list.presentation.exception.ExceptionHandler
import net.asere.omni.mvi.sample.list.presentation.exception.coroutineExceptionHandler
import net.asere.omni.mvi.sample.shared.core.extension.requireMessage
import net.asere.omni.mvi.sample.shared.domain.extension.empty
import net.asere.omni.mvi.sample.shared.presentation.model.asPresentation
import net.asere.omni.mvi.saveableStateContainer
import net.asere.omni.mvi.stateContainer
import net.asere.omni.mvi.unlockIntent

class ListViewModel(
    savedStateHandle: SavedStateHandle,
    exceptionHandler: ExceptionHandler,
    private val getRepositories: GetRepositories,
    private val searchRepositories: SearchRepositories,
) : ViewModel(),
    ActionContainerHost<ListState, ListEffect, ListAction>,
    LockContainerHost<ListState, ListEffect>,
    OverrideContainerHost<ListState, ListEffect> {

    companion object {
        private const val QUERY_DELAY = 300L
    }

    override val container = saveableStateContainer(
        initialState = ListState(),
        savedStateHandle = savedStateHandle,
        coroutineScope = viewModelScope,
        coroutineExceptionHandler = coroutineExceptionHandler(exceptionHandler)
    ).buildLockContainer()
        .buildOverrideContainer()
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
        reduce { copy(query = value, currentPage = 1) }
        delay(QUERY_DELAY) // Apply a delay to the intent to reduce query rate
        onError { showError(it) } // Executed when an error occurs
        reduce { copy(loading = true) }
        val repos = searchRepositories(query = value, currentState.currentPage)
        reduce { copy(loading = false, items = repos.items.map { it.asPresentation() }) }
    }

    // We are using lockIntent since we want this intent to execute only once at a time
    private fun fetchContent() = lockIntent {
        onError { showError(it) } // This block is called when the intent execution fails
        reduce { copy(loading = items.isEmpty(), error = String.empty()) }
        val repos = getRepositories(currentState.currentPage)
        reduce {
            copy(
                loading = false,
                currentPage = repos.currentPage,
                items = items + repos.items.map { it.asPresentation() }
            )
        }
        if (repos.items.isEmpty()) {
            lockIntent() // Lock this intent as the end of the list has been reached
        }
    }

    private fun showError(throwable: Throwable) = intent {
        reduce { copy(loading = false, error = throwable.requireMessage()) }
        post(ListEffect.ShowMessage(throwable.requireMessage()))
    }

    private fun retry() = intent {
        unlockIntent() // Unlock intent to allow content fetch
        fetchContent()
    }

    // Intentionally set public for demonstration purposes
    fun nextPage() = intent {
        reduce { copy(currentPage = currentPage + 1) }
        fetchContent()
    }

    // Intentionally created for test purposes
    fun continuesEmit() = intent {
        while (true) {
            reduce { copy(loading = false) }
            delay(200)
        }
    }

    fun continuesPost() = intent {
        while (true) {
            post(ListEffect.ShowMessage("List is full"))
            delay(200)
        }
    }
}