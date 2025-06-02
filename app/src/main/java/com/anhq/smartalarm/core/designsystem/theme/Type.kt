package com.anhq.smartalarm.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

val Typography.headline1: TextStyle
    get() = TextStyle(
        fontFamily = OxygenBold,
        fontSize = 32.sp,
        lineHeight = 48.sp,
        letterSpacing = 0.sp
    )

val Typography.headline2: TextStyle
    get() = TextStyle(
        fontFamily = OxygenBold,
        fontSize = 28.sp,
        lineHeight = 42.sp,
        letterSpacing = 0.sp
    )

val Typography.headline3: TextStyle
    get() = TextStyle(
        fontFamily = OxygenBold,
        fontSize = 24.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    )

val Typography.title1: TextStyle
    get() = TextStyle(
        fontFamily = OxygenRegular,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    )

val Typography.title2: TextStyle
    get() = TextStyle(
        fontFamily = OxygenRegular,
        fontSize = 20.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    )

val Typography.body1: TextStyle
    get() = TextStyle(
        fontFamily = OxygenRegular,
        fontSize = 24.sp,
        lineHeight = 27.sp,
        letterSpacing = 0.2.sp
    )

val Typography.body2: TextStyle
    get() = TextStyle(
        fontFamily = OxygenRegular,
        fontSize = 22.sp,
        lineHeight = 27.sp,
        letterSpacing = 0.2.sp
    )

val Typography.body3_bold: TextStyle
    get() = TextStyle(
        fontFamily = OxygenBold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    )

val Typography.body3_medium: TextStyle
    get() = TextStyle(
        fontFamily = OxygenRegular,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    )

val Typography.body4: TextStyle
    get() = TextStyle(
        fontFamily = OxygenRegular,
        fontSize = 18.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.1.sp
    )

val Typography.body5: TextStyle
    get() = TextStyle(
        fontFamily = OxygenRegular,
        fontSize = 16.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.1.sp
    )

val Typography.label1: TextStyle
    get() = TextStyle(
        fontFamily = OxygenBold,
        fontSize = 20.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    )

val Typography.label2: TextStyle
    get() = TextStyle(
        fontFamily = OxygenBold,
        fontSize = 16.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    )

val Typography.label3: TextStyle
    get() = TextStyle(
        fontFamily = OxygenBold,
        fontSize = 14.sp,
        lineHeight = 16.5.sp,
        letterSpacing = 0.5.sp
    )

val Typography.gradient1: TextStyle
    get() = TextStyle(
        brush = Brush.horizontalGradient(listOf(Pure01, Pure02)),
        fontFamily = OxygenBold,
        fontSize = 16.sp,
        lineHeight = 27.sp,
        letterSpacing = 0.2.sp
    )