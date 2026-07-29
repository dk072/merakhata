package com.merakhata.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Clean & Crisp Khatabook / OkCredit Style Palette

// Primary Brand Colors
val EmeraldPrimary = Color(0xFF059669)      // Crisp Khatabook Emerald
val DeepEmerald = Color(0xFF047857)         // Deep Forest Green
val LightEmeraldBg = Color(0xFFECFDF5)      // Light Mint Surface Tint

// Neutral Surfaces & Backgrounds (Crisp & High Contrast)
val BackgroundLight = Color(0xFFF8FAFC)     // Soft Off-White Background
val SurfaceLight = Color(0xFFFFFFFF)        // Pure Crisp White Surface

val DeepCharcoal = Color(0xFF0F172A)        // Deep Charcoal for Crisp Readable Text
val MediumSlate = Color(0xFF475569)         // Secondary Text Slate
val LightSlate = Color(0xFF64748B)          // Subtitle / Date Muted Text

val CardBorderLight = Color(0xFFE2E8F0)     // Crisp Subtle Border

// Financial Transaction Colors (Khatabook Style)
val ReceivableGreen = Color(0xFF059669)     // You Receive / Credit (Vibrant Green)
val LightReceivableBg = Color(0xFFECFDF5)   // Light Green Surface Tint

val PayableRed = Color(0xFFDC2626)          // You Give / Debit (Vibrant Red)
val LightPayableBg = Color(0xFFFEF2F2)      // Light Red Surface Tint

val SettledGray = Color(0xFF64748B)         // Settled Gray
val WarmGold = Color(0xFFD97706)            // Pending Alert Gold

// Gradients
val PrimaryHeaderGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF047857), Color(0xFF059669), Color(0xFF0D9488))
)

val ActionGaveGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFDC2626), Color(0xFFEF4444))
)

val ActionGotGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF047857), Color(0xFF059669))
)

// Legacy Compatibility Definitions
val HeaderGradientStart = Color(0xFF047857)
val HeaderGradientCenter = Color(0xFF059669)
val HeaderGradientEnd = Color(0xFF0D9488)

val GreenPrimary = EmeraldPrimary
val GreenLight = LightReceivableBg
val GreenDark = DeepEmerald
val GreenReceivable = ReceivableGreen
val RedPayable = PayableRed
val RedLight = LightPayableBg
val PrimaryAccent = EmeraldPrimary
val SecondaryAccent = Color(0xFF0284C7)
val BackgroundDark = Color(0xFF0F172A)
val SurfaceDark = Color(0xFF1E293B)
val CardBorderDark = Color(0xFF334155)
