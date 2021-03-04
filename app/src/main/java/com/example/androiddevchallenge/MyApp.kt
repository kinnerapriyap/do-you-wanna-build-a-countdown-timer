package com.example.androiddevchallenge

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.androiddevchallenge.ui.theme.MyTheme

var bounds: StretchableSquareBounds = StretchableSquareBounds()

@Composable
fun MyApp() {
    val isDebug = false
    val size = 300f

    var screenHeight by remember { mutableStateOf(0) }
    val dragRange = screenHeight - size

    var yCoordinate by remember { mutableStateOf(0f) }
    var absTranslation by remember { mutableStateOf(0f) }
    var scaleForTranslation by remember { mutableStateOf(1f) }
    var potentiallyAtTop by remember { mutableStateOf(true) }

    Scaffold {
        Text(
            text = "yCoordinate: $yCoordinate\n" +
                    "absTranslation: $absTranslation\n" +
                    "potentiallyAtTop: $potentiallyAtTop\n",
            modifier = Modifier.padding(20.dp)
        )
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
                .offset { IntOffset(0, yCoordinate.toInt()) }
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        potentiallyAtTop = isInTopHalf(yCoordinate, size, screenHeight)
                        val newCoordinate = yCoordinate + delta
                        yCoordinate =
                            if (newCoordinate < 0 || newCoordinate > dragRange) yCoordinate
                            else yCoordinate + delta
                        absTranslation =
                            if (potentiallyAtTop) yCoordinate else screenHeight - yCoordinate
                        scaleForTranslation = 1 + absTranslation / size
                    },
                    onDragStopped = {
                        potentiallyAtTop = isInTopHalf(yCoordinate, size, screenHeight)
                        yCoordinate = if (potentiallyAtTop) 0f else dragRange
                        absTranslation = 0f
                        scaleForTranslation = 1f
                    }
                ),
            contentAlignment = BiasAlignment(-0.3f, -1f)
        ) {
            screenHeight = this.constraints.maxHeight
            Canvas(modifier = Modifier.wrapContentSize()) {
                val paintColorPair = Color(0xff33b5e5) to Color(0xffffbb33)
                bounds = bounds.copy(
                    stretchFactor = 0f,
                    startPositionX = 0f,
                    endPositionX = size,
                    startPositionY = 0f,
                    endPositionY = size * scaleForTranslation
                )
                if (!potentiallyAtTop && !isDebug) scale(1f, -1f) {}
                drawPath(
                    path = bounds.getStretchableSquarePath(isDebug),
                    style = if (isDebug) Stroke(width = 3f) else Fill,
                    color = when {
                        isDebug -> Color.Black
                        potentiallyAtTop -> paintColorPair.first
                        else -> paintColorPair.second
                    }
                )
            }
        }
    }
}

fun isInTopHalf(yTranslation: Float, size: Float, screenHeight: Int) =
    yTranslation + size / 2 <= screenHeight / 2f

@Preview
@Composable
fun MyAppPreview() {
    MyTheme {
        MyApp()
    }
}