package com.madapp.omni.mvi.sample.list.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.madapp.omni.mvi.sample.R

@Composable
fun RetryComponent(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(all = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(
                    id = R.string.fetch_error, errorMessage
                ),
                style = TextStyle(
                    color = Color.Black
                )
            )
            Button(
                onClick = {
                    onRetry()
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Blue
                ),
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(id = R.string.try_again),
                    style = TextStyle(
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RetryComponentPreview() {
    RetryComponent(
        errorMessage = "Some error",
        onRetry = {}
    )
}