package net.asere.omni.mvi.sample.list.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import net.asere.omni.mvi.OnEffect
import net.asere.omni.mvi.on
import net.asere.omni.mvi.sample.list.presentation.component.LoadingComponent
import net.asere.omni.mvi.sample.list.presentation.component.RepoItem
import net.asere.omni.mvi.sample.list.presentation.component.RetryComponent
import net.asere.omni.mvi.sample.list.presentation.extension.showSnackbar
import net.asere.omni.mvi.state
import org.koin.androidx.compose.getViewModel

@Composable
fun ListScreen(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    viewModel: ListViewModel = getViewModel()
) {
    val state by viewModel.state()
    viewModel.OnEffect {
        when (it) {
            is ListEffect.ShowMessage -> scaffoldState.showSnackbar(it.text)
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        backgroundColor = Color.White
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(state.items) { index, item ->
                if (index >= state.items.lastIndex - 10) {
                    viewModel.on(ListAction.NextPage) // Call next page intent
                }
                RepoItem(repo = item)
            }
        }
        if (state.loading) {
            LoadingComponent()
        }
        if (state.error.isNotEmpty()) {
            RetryComponent(
                errorMessage = state.error,
                onRetry = { viewModel.on(ListAction.Retry) } // Call next page intent
            )
        }
    }
}