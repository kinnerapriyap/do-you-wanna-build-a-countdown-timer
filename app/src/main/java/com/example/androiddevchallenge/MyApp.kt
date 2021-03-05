/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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

    var screenHeight by remember { mutableStateOf(0) }
    val dragRange = screenHeight - size

    val stickyThreshold =
        if (potentiallyAtTop) screenHeight / 3f - size / 2
        else screenHeight / 3f
    val offset =
        when {
            stuck && potentiallyAtTop -> 0
            stuck && !potentiallyAtTop -> dragRange.toInt()
            else -> yCoordinate.toInt()
        }

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
                            if (potentiallyAtTop) yCoordinate
                            else screenHeight - yCoordinate - size
                        if (stuck) stuck = absTranslation < stickyThreshold
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
                    stretchFactor = if (stuck) absTranslation / stickyThreshold else 0f,
                    startPositionX = 0f,
                    endPositionX = size,
                    startPositionY = 0f,
                    endPositionY = if (stuck) size + absTranslation else size
                )
                scale(
                    scaleX = 1f,
                    scaleY = if (!potentiallyAtTop && !isDebug) -1f else 1f,
                    pivot = Offset(size / 2, size / 2)
                ) {
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
