package com.merakhata.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Premium High-Contrast Vibrant Palettes
val ElectricEmerald = Color(0xFF10B981)   // Vibrant Emerald Green
val NeonMint = Color(0xFF34D399)          // Ultra-Crisp Mint for Dark Mode
val DeepEmerald = Color(0xFF047857)       // Rich Deep Emerald for Light Mode

val CoralRose = Color(0xFFF43F5E)         // Vibrant Payable Rose
val LightRoseBg = Color(0xFFFFF1F2)       // Light Rose Tint
val DarkRoseBg = Color(0xFF3F121C)        // Dark Mode Rose Surface Tint

val HeaderGradientStart = Color(0xFF064E3B) // Deep Emerald
val HeaderGradientCenter = Color(0xFF047857) // Rich Emerald
val HeaderGradientEnd = Color(0xFF0D9488)   // Vibrant Teal

val PrimaryHeaderGradient = Brush.horizontalGradient(
    colors = listOf(HeaderGradientStart, HeaderGradientCenter, HeaderGradientEnd)
)

val ActionGaveGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFE11D48), Color(0xFFF43F5E))
)

val ActionGotGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF059669), Color(0xFF10B981))
)

// Legacy compatibility definitions with High-Contrast values
val GreenPrimary = Color(0xFF10B981)
val GreenLight = Color(0xFFECFDF5)
val GreenDark = Color(0xFF064E3B)

val RedPayable = Color(0xFFF43F5E)
val RedLight = Color(0xFFFFF1F2)

val GreenReceivable = Color(0xFF10B981)
val SettledGray = Color(0xFF94A3B8)

val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)

val BackgroundDark = Color(0xFF0B132B)    // Deep Rich Midnight Dark
val SurfaceDark = Color(0xFF1C2541)       // Vibrant Slate Midnight Surface

val PrimaryAccent = Color(0xFF10B981)
val SecondaryAccent = Color(0xFF06B6D4)

val CardBorderLight = Color(0xFFE2E8F0)
val CardBorderDark = Color(0xFF334155)
