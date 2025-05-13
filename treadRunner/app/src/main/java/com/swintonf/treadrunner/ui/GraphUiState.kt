package com.swintonf.treadrunner.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset

data class GraphUiState(
    val graphPoints: MutableList<Offset> = mutableStateListOf(Offset(0f,0f))

)
