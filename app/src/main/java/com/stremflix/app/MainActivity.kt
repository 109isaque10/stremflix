package com.stremflix.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.stremflix.commonui.theme.StremFlixTheme
import com.stremflix.app.navigation.AppNavigation // Placeholder for next step
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StremFlixTheme {
                Surface {
                    // Navigation will be implemented in Part 2
                    AppNavigation() 
                }
            }
        }
    }
}