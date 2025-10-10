package net.asere.omni.mvi.sample.list.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import net.asere.omni.mvi.OnEffect
import net.asere.omni.mvi.sample.R
import net.asere.omni.mvi.sample.list.presentation.component.LoadingComponent
import net.asere.omni.mvi.sample.list.presentation.component.RepoItem
import net.asere.omni.mvi.sample.list.presentation.component.RetryComponent
import net.asere.omni.mvi.sample.list.presentation.extension.showSnackbar
import net.asere.omni.mvi.state
import org.koin.androidx.compose.koinViewModel

@Composable
fun ListScreen(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    viewModel: ListViewModel = koinViewModel()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = state.query.orEmpty(),
                onValueChange = { value -> viewModel.on(ListAction.Query(value)) },
                singleLine = true,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.search_placeholder),
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.Black,
                    disabledTextColor = Color.Transparent,
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                maxLines = 1
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
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
}