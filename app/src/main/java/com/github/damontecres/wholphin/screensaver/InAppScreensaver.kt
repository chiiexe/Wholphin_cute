package com.github.damontecres.wholphin.screensaver

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun InAppScreensaver(
    viewModel: InteractionTrackerViewModel,
    dreamHostViewModel: DreamHostViewModel = hiltViewModel()
) {
    val isVisible by viewModel.isScreensaverVisible.collectAsState()
    val backdropUrl by dreamHostViewModel.backdropUrl.collectAsState()

    if (isVisible) {
        Dialog(
            onDismissRequest = { viewModel.notifyInteraction() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { viewModel.notifyInteraction() }
                    )
            ) {
                DreamHost(imageUrl = backdropUrl)
            }
        }
    }
}
