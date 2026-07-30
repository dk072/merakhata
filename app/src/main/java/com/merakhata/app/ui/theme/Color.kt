package com.merakhata.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Modern Mera Khata Design Tokens (From HTML / Tailwind Spec)

val SurfaceBg = Color(0xFFF8F9FF)
val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceContainerLow = Color(0xFFEFF4FF)
val SurfaceContainer = Color(0xFFE5EEFF)
val SurfaceContainerHigh = Color(0xFFDCE9FF)
val SurfaceContainerHighest = Color(0xFFD3E4FE)

val PrimaryDark = Color(0xFF000000)
val PrimaryContainerNavy = Color(0xFF131B2E)
val OnPrimaryContainer = Color(0xFF7C839B)

// Secondary (You Got / Credit)
val SecondaryTeal = Color(0xFF006C49)
val SecondaryContainerMint = Color(0xFF6CF8BB)
val OnSecondaryContainerTeal = Color(0xFF00714D)
val SecondaryFixedMint = Color(0xFF6FFBBE)
val SecondaryFixedDimMint = Color(0xFF4EDEA3)

// Error (You Gave / Debit)
val ErrorRed = Color(0xFFBA1A1A)
val ErrorContainerPink = Color(0xFFFFDAD6)
val OnErrorContainerRed = Color(0xFF93000A)

// Text & Outline Tokens
val OnSurfaceDark = Color(0xFF0B1C30)
val OnSurfaceVariantGray = Color(0xFF45464D)
val OutlineGray = Color(0xFF76777D)
val OutlineVariantLight = Color(0xFFC6C6CD)

// Standard Aliases for compatibility
val EmeraldPrimary = SecondaryTeal
val DeepEmerald = Color(0xFF005236)
val LightEmeraldBg = SecondaryContainerMint

val BackgroundLight = SurfaceBg
val SurfaceLight = SurfaceContainerLowest

val DeepCharcoal = OnSurfaceDark
val MediumSlate = OnSurfaceVariantGray
val LightSlate = OutlineGray

val CardBorderLight = OutlineVariantLight

val ReceivableGreen = SecondaryTeal
val LightReceivableBg = SecondaryContainerMint

val PayableRed = ErrorRed
val LightPayableBg = ErrorContainerPink

val SettledGray = OutlineGray
val WarmGold = Color(0xFFD97706)

// Gradients
val PrimaryHeaderGradient = Brush.horizontalGradient(
    colors = listOf(PrimaryContainerNavy, Color(0xFF1E293B))
)

val ActionGaveGradient = Brush.horizontalGradient(
    colors = listOf(ErrorRed, Color(0xFFD92D2D))
)

val ActionGotGradient = Brush.horizontalGradient(
    colors = listOf(SecondaryTeal, Color(0xFF00895D))
)

val HeaderGradientStart = PrimaryContainerNavy
val HeaderGradientCenter = PrimaryContainerNavy
val HeaderGradientEnd = PrimaryContainerNavy

val GreenPrimary = SecondaryTeal
val GreenLight = LightReceivableBg
val GreenDark = DeepEmerald
val GreenReceivable = ReceivableGreen
val RedPayable = PayableRed
val RedLight = LightPayableBg
val PrimaryAccent = SecondaryTeal
val SecondaryAccent = Color(0xFF0284C7)
val BackgroundDark = Color(0xFF0F172A)
val SurfaceDark = Color(0xFF1E293B)
val CardBorderDark = Color(0xFF334155)

