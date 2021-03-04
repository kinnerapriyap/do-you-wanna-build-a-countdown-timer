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

    var yCoordinate by remember { mutableStateOf(0f) }
    var absTranslation by remember { mutableStateOf(0f) }

    var potentiallyAtTop by remember { mutableStateOf(true) }
    var stuck by remember { mutableStateOf(true) }
    var justUnstuck by remember { mutableStateOf(true) }

    var screenHeight by remember { mutableStateOf(0) }
    val dragRange = screenHeight - size

    val stickyThreshold =
        if (potentiallyAtTop) screenHeight / 3f
        else screenHeight / 3f + size / 2f
    val offset = if (stuck && potentiallyAtTop) 0 else yCoordinate.toInt()

    Scaffold {
        Text(
            text = "yCoord: $yCoordinate\n" +
                    "absT: $absTranslation\n" +
                    "pAtTop: $potentiallyAtTop\n" +
                    "sThreshold: $stickyThreshold\n" +
                    "stuck: $stuck\n",
            modifier = Modifier.padding(20.dp)
        )
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
                .offset { IntOffset(0, offset) }
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
                        if (stuck) {
                            stuck = absTranslation < stickyThreshold
                            justUnstuck = !stuck
                        }
                    },
                    onDragStopped = {
                        potentiallyAtTop = isInTopHalf(yCoordinate, size, screenHeight)
                        yCoordinate = if (potentiallyAtTop) 0f else dragRange
                        absTranslation = 0f
                        stuck = true
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
                    endPositionY = if (stuck) size + absTranslation else size
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