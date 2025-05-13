package com.swintonf.treadrunner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import com.swintonf.treadrunner.ui.GraphScreen
import com.swintonf.treadrunner.ui.theme.TreadRunnerTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TreadRunnerTheme {
                GraphScreen(context = LocalContext.current)
            }
        }
    }
}



