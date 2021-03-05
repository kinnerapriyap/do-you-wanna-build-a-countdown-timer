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

data class StretchableSquareBounds(
    val startPositionX: Float = 0f,
    val endPositionX: Float = 0f,
    val startPositionY: Float = 0f,
    val endPositionY: Float = 0f,
    val stretchFactor: Float = 1f
) {
    private val incrementValueX = (endPositionX - startPositionX) / 10
    private val incrementValueY = (endPositionY - startPositionY) / 10
    private val midLeftPointX = startPositionX + 4 * incrementValueX
    private val midRightPointX = startPositionX + 6 * incrementValueX
    private val controlPointY = startPositionY + 1 * incrementValueY

    val topLeadingPoint: PointF = PointF(startPositionX, startPositionY)
    val topTrailingPoint: PointF = PointF(endPositionX, startPositionY)
    val controlPointToBottomTrailing: PointF =
        getPointFByFactor(
            PointF(endPositionX, controlPointY),
            PointF(midRightPointX, controlPointY),
            stretchFactor
        )
    val bottomTrailingPoint: PointF =
        getPointFByFactor(
            PointF(endPositionX, endPositionY),
            PointF(midRightPointX, endPositionY),
            stretchFactor
        )
    val bottomLeadingPoint: PointF =
        getPointFByFactor(
            PointF(startPositionX, endPositionY),
            PointF(midLeftPointX, endPositionY),
            stretchFactor
        )
    val controlPointToTopLeading: PointF =
        getPointFByFactor(
            PointF(startPositionX, controlPointY),
            PointF(midLeftPointX, controlPointY),
            stretchFactor
        )

    private fun getPointFByFactor(startPoint: PointF, endPoint: PointF, factor: Float): PointF =
        PointF(
            startPoint.x + factor * (endPoint.x - startPoint.x),
            startPoint.y + factor * (endPoint.y - startPoint.y)
        )
}
