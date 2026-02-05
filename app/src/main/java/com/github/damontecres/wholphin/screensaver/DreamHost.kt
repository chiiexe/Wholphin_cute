package com.github.damontecres.wholphin.screensaver

import android.graphics.Bitmap
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap

@Composable
fun DreamHost(imageUrl: String?) {
    var loadedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val bgAlpha = remember { Animatable(0f) }
    val context = LocalContext.current

    // fade to black to stop it being so harsh
    LaunchedEffect(Unit) {
        bgAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500) // 1 sec
        )
    }

    LaunchedEffect(imageUrl) {
        if (imageUrl == null) {
            loadedBitmap = null
            return@LaunchedEffect
        }

        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .build()

        val result = context.imageLoader.execute(request)

        if (result is SuccessResult) {
            loadedBitmap = result.image.toBitmap()
        }
    }

    // apply animated result to background box
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = bgAlpha.value))
    ) {
        Crossfade(
            targetState = loadedBitmap,
            animationSpec = tween(durationMillis = 2000),
            label = "ImageCrossfade"
        ) { bitmap ->
            if (bitmap != null) {
                val scale = remember { Animatable(1f) }

                LaunchedEffect(bitmap) {
                    scale.snapTo(1f) // lowered zoom even more
                    scale.animateTo(
                        targetValue = 1.1f,
                        animationSpec = tween(
                            durationMillis = 60000, //1 minutos
                            easing = LinearEasing
                        )
                    )
                }

                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                        },
                    contentScale = ContentScale.Crop
                )
            } else {
                // empty box as placeholder
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}