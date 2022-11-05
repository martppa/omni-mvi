package com.madapp.omni.mvi.sample.list.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.madapp.omni.mvi.sample.list.presentation.component.LoadingComponent
import com.madapp.omni.mvi.sample.list.presentation.component.RepoItem
import com.madapp.omni.mvi.sample.list.presentation.component.RetryComponent
import org.koin.androidx.compose.getViewModel

@Composable
fun ListScreen(
    viewModel: ListViewModel = getViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        backgroundColor = Color.White
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(state.items) { index, item ->
                if (index == state.items.lastIndex) {
                    viewModel.nextPage()
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
                onRetry = { viewModel.onRetry() }
            )
        }
    }
}