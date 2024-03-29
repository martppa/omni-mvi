package net.asere.omni.mvi.sample.list.presentation.component

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
import net.asere.omni.mvi.sample.shared.domain.model.Repo
import net.asere.omni.mvi.sample.shared.presentation.model.RepoModel

@Composable
fun RepoItem(repo: RepoModel) {
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