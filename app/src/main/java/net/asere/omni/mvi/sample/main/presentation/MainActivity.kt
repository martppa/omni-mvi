package net.asere.omni.mvi.sample.main.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import net.asere.omni.mvi.sample.shared.presentation.theme.OmniMviSample

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OmniMviSample {
                MainScreen()
            }
        }
    }
}