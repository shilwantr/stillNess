package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.viewmodel.WellnessViewModel

@Composable
fun TrendsScreen(
    viewModel: WellnessViewModel,
    modifier: Modifier = Modifier
) {
    JournalScreen(viewModel = viewModel, modifier = modifier)
}
