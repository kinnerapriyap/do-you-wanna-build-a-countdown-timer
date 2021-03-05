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

import android.graphics.PointF
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path

fun Path.moveTo(point: PointF) = moveTo(point.x, point.y)

fun Path.lineTo(point: PointF) = lineTo(point.x, point.y)

fun Path.quadTo(controlPoint: PointF, endPoint: PointF) =
    quadraticBezierTo(controlPoint.x, controlPoint.y, endPoint.x, endPoint.y)

fun Path.addPoint(point: PointF) =
    addOval(Rect(point.toOffset(), 5f))

fun PointF.toOffset() = Offset(x, y)

fun StretchableSquareBounds.getStretchableSquarePath(isDebug: Boolean) =
    Path().apply {
        moveTo(topLeadingPoint)
        lineTo(topTrailingPoint)

        if (isDebug) {
            lineTo(controlPointToBottomTrailing)
            addPoint(controlPointToBottomTrailing)
            moveTo(topTrailingPoint)
        }
        quadTo(controlPointToBottomTrailing, bottomTrailingPoint)

        lineTo(bottomLeadingPoint)

        if (isDebug) {
            lineTo(controlPointToTopLeading)
            addPoint(controlPointToTopLeading)
            moveTo(bottomLeadingPoint)
        }
        quadTo(controlPointToTopLeading, topLeadingPoint)
    }
