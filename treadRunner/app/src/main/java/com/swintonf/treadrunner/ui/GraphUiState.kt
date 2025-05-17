package com.swintonf.treadrunner.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset

data class GraphUiState(
    val portraitGraphPoints: MutableList<Offset> = mutableStateListOf(Offset(0f,0f)),
    val landscapeGraphPoints: MutableList<Offset> = mutableStateListOf(Offset(0f,0f)),
    val GraphPoints: MutableList<Offset> = mutableStateListOf(Offset(0f,0f))


)
