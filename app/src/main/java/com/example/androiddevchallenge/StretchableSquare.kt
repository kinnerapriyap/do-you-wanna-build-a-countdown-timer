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

import android.content.Context
import android.os.CountDownTimer
import android.widget.Toast
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateInt
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

enum class BoxState {
    JustUnstuck,
    Stuck,
    Normal
}

@Composable
fun StretchableSquare() {
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

    var timer by remember { mutableStateOf("") }
    val timerText = absTranslation.let {
        return@let if (stuck) it.toInt().toMinSecString() else "0 : 0"
    }
    var countDownTimer by remember { mutableStateOf<CountDownTimer?>(null) }

    val paintColorPair = Color(0xff33b5e5) to Color(0xffffbb33)
    val color = remember { Animatable(paintColorPair.first) }
    LaunchedEffect(potentiallyAtTop) {
        color.animateTo(
            if (potentiallyAtTop) paintColorPair.first
            else paintColorPair.second
        )
    }

    var currentState by remember { mutableStateOf(BoxState.Stuck) }
    val transition = updateTransition(currentState)
    val stretchFactor by transition.animateFloat(
        transitionSpec = {
            when {
                BoxState.Stuck isTransitioningTo BoxState.JustUnstuck ->
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                else -> tween(durationMillis = 100)
            }
        }
    ) { state ->
        when (state) {
            BoxState.JustUnstuck -> 0f
            BoxState.Stuck -> absTranslation / stickyThreshold
            BoxState.Normal -> 0f
        }
    }
    val yOffset by transition.animateInt(
        transitionSpec = {
            when {
                BoxState.JustUnstuck isTransitioningTo BoxState.Normal ->
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                else -> tween(durationMillis = 1)
            }
        }
    ) { state ->
        when (state) {
            BoxState.JustUnstuck -> yCoordinate.toInt()
            BoxState.Stuck -> if (potentiallyAtTop) 0 else dragRange.toInt()
            BoxState.Normal -> yCoordinate.toInt()
        }
    }
    val context = LocalContext.current

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
            .offset { IntOffset(0, yOffset) }
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
                    if (stuck) {
                        stuck = absTranslation < stickyThreshold
                        if (!stuck) {
                            showToast(context, "Oops, you just cancelled your timer!")
                            countDownTimer?.cancel()
                            countDownTimer = null
                        }
                        currentState = if (stuck) BoxState.Stuck else BoxState.JustUnstuck
                    }
                },
                onDragStopped = {
                    currentState = BoxState.Normal
                    potentiallyAtTop = isInTopHalf(yCoordinate, size, screenHeight)
                    yCoordinate = if (potentiallyAtTop) 0f else dragRange
                    if (stuck && countDownTimer == null)
                        countDownTimer =
                            object : CountDownTimer(absTranslation.toInt() * 1000L, 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    timer = (millisUntilFinished / 1000).toInt().toMinSecString()
                                }

                                override fun onFinish() {
                                    timer = "Done!"
                                }
                            }.start()
                    absTranslation = 0f
                    stuck = true
                }
            ),
        contentAlignment = BiasAlignment(-0.3f, -1f)
    ) {
        Text(
            text = if (stuck && countDownTimer != null) timer else timerText,
            modifier = Modifier.fillMaxWidth().padding(40.dp)
        )
        Button(
            onClick = {
                countDownTimer?.cancel()
                countDownTimer = null
            },
            modifier = Modifier.wrapContentSize()
                .offset { IntOffset(constraints.maxWidth / 2, size.toInt() / 3) },
            colors = ButtonDefaults.buttonColors(backgroundColor = color.value)
        ) {
            Text(text = "Stop")
        }
        screenHeight = this.constraints.maxHeight
        Canvas(modifier = Modifier.wrapContentSize()) {
            val bounds = StretchableSquareBounds(
                stretchFactor = stretchFactor,
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
                        else -> color.value
                    }
                )
            }
        }
    }
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun Int.toMinSecString(): String {
    var no = this
    val min = no / 60
    no -= min * 60
    val sec = no
    return "$min : $sec"
}

fun isInTopHalf(yTranslation: Float, size: Float, screenHeight: Int) =
    yTranslation + size / 2 <= screenHeight / 2f
