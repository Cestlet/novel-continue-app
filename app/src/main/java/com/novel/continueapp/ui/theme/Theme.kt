package com.novel.continueapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── 配色（靛蓝紫渐变风格） ──

val Primary = Color(0xFF5B5FEF)          // 主色：靛蓝
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFE3E5FF)
val OnPrimaryContainer = Color(0xFF1A1B6B)
val Secondary = Color(0xFF9C6ADE)        // 辅助：紫
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFF0E4FF)
val OnSecondaryContainer = Color(0xFF3A1D63)
val Tertiary = Color(0xFF4BA3E3)         // 点缀：天蓝
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFD5ECFF)
val OnTertiaryContainer = Color(0xFF0F3A5E)

val Background = Color(0xFFF7F7FC)       // 页面背景：浅灰白
val OnBackground = Color(0xFF1C1B22)
val Surface = Color(0xFFFFFFFF)
val OnSurface = Color(0xFF1C1B22)
val SurfaceVariant = Color(0xFFEFEFF5)
val OnSurfaceVariant = Color(0xFF5A5A66)
val SurfaceTint = Primary
val Outline = Color(0xFFC4C4D0)
val OutlineVariant = Color(0xFFE4E4EC)

val Error = Color(0xFFB3261E)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF410002)

private val NovelColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceTint = SurfaceTint,
    outline = Outline,
    outlineVariant = OutlineVariant,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer
)

// ── 形状：大圆角现代风 ──

private val NovelShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

// ── 排版 ──

private val NovelTypography = Typography(
    titleLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)

@Composable
fun NovelTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NovelColorScheme,
        shapes = NovelShapes,
        typography = NovelTypography,
        content = content
    )
}