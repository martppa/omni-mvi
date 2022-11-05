package com.madapp.omni.mvi.sample.list.presentation.extension

import androidx.compose.material.ScaffoldState

suspend fun ScaffoldState.showSnackbar(message: String) = snackbarHostState.showSnackbar(message)