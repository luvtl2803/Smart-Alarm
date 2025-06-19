package com.anhq.smartalarm.core.designsystem.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

val headline1: TextStyle
    get() = TextStyle(
        fontFamily = NotoSansBold,
        fontSize = 26.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.sp
    )

val headline2: TextStyle
    get() = TextStyle(
        fontFamily = NotoSansBold,
        fontSize = 22.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    )

val headline3: TextStyle
    get() = TextStyle(
        fontFamily = NotoSansBold,
        fontSize = 20.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    )

val title1: TextStyle
    get() = TextStyle(
        fontFamily = NotoSansBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.1.sp
    )

val title2: TextStyle
    get() = TextStyle(
        fontFamily = NotoSansBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.1.sp
    )

val title3: TextStyle
    get() = TextStyle(
        fontFamily = NotoSansBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    )

val body1: TextStyle
    get() = TextStyle(
        fontFamily = NotoSansRegular,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.15.sp
    )

val body2: TextStyle
    get() = TextStyle(
        fontFamily = NotoSansRegular,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    )

val body3_bold: TextStyle
    get() = TextStyle(
        fontFamily = NotoSansBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    )

val body3_medium: TextStyle
    get() = TextStyle(
        fontFamily = NotoSansRegular,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    )

val body4: TextStyle
    get() = TextStyle(
        fontFamily = NotoSansRegular,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )

val body5: TextStyle
    get() = TextStyle(
        fontFamily = NotoSansRegular,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    )

val label1: TextStyle
    get() = TextStyle(
        fontFamily = NotoSansBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    )

val label2: TextStyle
    get() = TextStyle(
        fontFamily = NotoSansBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )

val label3: TextStyle
    get() = TextStyle(
        fontFamily = NotoSansBold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )

val gradient1: TextStyle
    get() = TextStyle(
        brush = Brush.horizontalGradient(listOf(Pure01, Pure02)),
        fontFamily = NotoSansBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp
    )
