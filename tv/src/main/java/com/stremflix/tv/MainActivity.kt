package com.stremflix.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.stremflix.commonui.theme.StremFlixTheme
import com.stremflix.tv.navigation.TvNavigation // Placeholder for next step
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StremFlixTheme {
                // TV Navigation will be implemented in Part 2
                TvNavigation()
            }
        }
    }
}