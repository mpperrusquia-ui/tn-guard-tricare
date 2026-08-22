package org.tnguardtricare.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Mirrors content/design-tokens.json — keep in sync by hand when that file changes, same as
 * iOS's Theme.swift. Coyote-brown palette: no camo patterns, stencil fonts, or high-contrast
 * tactical UI, deliberately.
 */
object AppColors {
    // Light
    val PrimaryLight = Color(0xFF8A6E52)
    val PrimaryDarkLight = Color(0xFF6E5640)
    val AccentLight = Color(0xFF5B6B58)
    val BackgroundLight = Color(0xFFF7F3EC)
    val SurfaceLight = Color(0xFFFFFFFF)
    val SurfaceAltLight = Color(0xFFEFE7DA)
    val TextPrimaryLight = Color(0xFF2B2620)
    val TextSecondaryLight = Color(0xFF6B6255)
    val SuccessLight = Color(0xFF4C7A54)
    val WarningLight = Color(0xFFB4762A)
    val DangerLight = Color(0xFFA6423A)
    val BorderLight = Color(0xFFE1D6C4)

    // Dark
    val PrimaryDark = Color(0xFFC9A97E)
    val PrimaryDarkDark = Color(0xFFA98A63)
    val AccentDark = Color(0xFF8FA089)
    val BackgroundDark = Color(0xFF1C1815)
    val SurfaceDark = Color(0xFF262019)
    val SurfaceAltDark = Color(0xFF33291F)
    val TextPrimaryDark = Color(0xFFF3EEE4)
    val TextSecondaryDark = Color(0xFFB8AB98)
    val SuccessDark = Color(0xFF7FB489)
    val WarningDark = Color(0xFFE0A85C)
    val DangerDark = Color(0xFFE08479)
    val BorderDark = Color(0xFF3C3226)
}

object AppSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}

object AppRadius {
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val pill = 999.dp
}

object AppTypography {
    val largeTitle = androidx.compose.ui.text.TextStyle(fontSize = 30.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    val title = androidx.compose.ui.text.TextStyle(fontSize = 22.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
    val headline = androidx.compose.ui.text.TextStyle(fontSize = 17.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
    val body = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Normal)
    val callout = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Normal)
    val caption = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Normal)
}

/** Extra semantic colors Material3's ColorScheme doesn't have slots for (success/warning/border). */
data class ExtendedColors(
    val success: Color,
    val warning: Color,
    val danger: Color,
    val border: Color,
    val surfaceAlt: Color,
)

val LocalExtendedColors = androidx.compose.runtime.staticCompositionLocalOf {
    ExtendedColors(
        success = AppColors.SuccessLight,
        warning = AppColors.WarningLight,
        danger = AppColors.DangerLight,
        border = AppColors.BorderLight,
        surfaceAlt = AppColors.SurfaceAltLight,
    )
}

@Composable
fun TNGuardTricareTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()

    val colorScheme = if (dark) {
        darkColorScheme(
            primary = AppColors.PrimaryDark,
            onPrimary = AppColors.BackgroundDark,
            secondary = AppColors.AccentDark,
            background = AppColors.BackgroundDark,
            onBackground = AppColors.TextPrimaryDark,
            surface = AppColors.SurfaceDark,
            onSurface = AppColors.TextPrimaryDark,
            surfaceVariant = AppColors.SurfaceAltDark,
            onSurfaceVariant = AppColors.TextSecondaryDark,
            error = AppColors.DangerDark,
            outline = AppColors.BorderDark,
        )
    } else {
        lightColorScheme(
            primary = AppColors.PrimaryLight,
            onPrimary = Color.White,
            secondary = AppColors.AccentLight,
            background = AppColors.BackgroundLight,
            onBackground = AppColors.TextPrimaryLight,
            surface = AppColors.SurfaceLight,
            onSurface = AppColors.TextPrimaryLight,
            surfaceVariant = AppColors.SurfaceAltLight,
            onSurfaceVariant = AppColors.TextSecondaryLight,
            error = AppColors.DangerLight,
            outline = AppColors.BorderLight,
        )
    }

    val extended = if (dark) {
        ExtendedColors(
            success = AppColors.SuccessDark,
            warning = AppColors.WarningDark,
            danger = AppColors.DangerDark,
            border = AppColors.BorderDark,
            surfaceAlt = AppColors.SurfaceAltDark,
        )
    } else {
        ExtendedColors(
            success = AppColors.SuccessLight,
            warning = AppColors.WarningLight,
            danger = AppColors.DangerLight,
            border = AppColors.BorderLight,
            surfaceAlt = AppColors.SurfaceAltLight,
        )
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalExtendedColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}
