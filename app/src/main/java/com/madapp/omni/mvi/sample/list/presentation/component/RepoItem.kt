package com.madapp.omni.mvi.sample.list.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.madapp.omni.mvi.sample.shared.domain.model.Repo

@Composable
fun RepoItem(repo: Repo) {
    Box(
        modifier = Modifier
            .background(
                if (repo.fork)
                    Color.Cyan
                else
                    Color.Transparent
            )
            .padding(all = 20.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = repo.name,
            style = TextStyle(
                color = Color.Black
            )
        )
    }
}