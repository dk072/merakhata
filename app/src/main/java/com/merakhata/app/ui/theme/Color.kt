package com.merakhata.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Premium Mobile Fintech Color Tokens

// Primary Branding & Actions
val DeepEmerald = Color(0xFF047857)         // Rich Deep Emerald Green
val EmeraldPrimary = Color(0xFF059669)      // Primary Action Emerald
val ElectricEmerald = Color(0xFF10B981)     // Vibrant Emerald Accent
val NeonMint = Color(0xFF34D399)            // Dark Mode Primary Mint Accent

// Secondary / Charcoal Contrast
val DeepCharcoal = Color(0xFF0F172A)        // Deep Charcoal for Titles & Strong Text
val SlateNavy = Color(0xFF1E293B)           // Slate Navy for Dark Surfaces
val MutedSlate = Color(0xFF64748B)          // Muted Secondary Text

// Warm Accent
val WarmGold = Color(0xFFF59E0B)            // Warm Gold Accent (Pending & Highlights)
val LightGoldBg = Color(0xFFFEF3C7)         // Light Gold Surface Tint

// Semantic Financial Colors
val ReceivableGreen = Color(0xFF10B981)     // You Will Receive / Credit
val LightReceivableBg = Color(0xFFECFDF5)   // Light Green Surface Tint
val DarkReceivableBg = Color(0xFF064E3B)    // Dark Green Surface Tint

val PayableRed = Color(0xFFEF4444)          // You Will Give / Debit
val LightPayableBg = Color(0xFFFEF2F2)      // Light Red Surface Tint
val DarkPayableBg = Color(0xFF451215)       // Dark Red Surface Tint

val PendingAmber = Color(0xFFF59E0B)        // Pending / Reminder Alert
val InfoBlue = Color(0xFF3B82F6)            // Informational Blue

// Neutral Backgrounds & Surfaces
val BackgroundLight = Color(0xFFF8FAFC)     // Soft Warm Neutral Off-White
val SurfaceLight = Color(0xFFFFFFFF)        // Pure White Surface

val BackgroundDark = Color(0xFF0B132B)      // Rich Midnight Slate Dark
val SurfaceDark = Color(0xFF1C2541)         // Elevated Slate Midnight Surface

val CardBorderLight = Color(0xFFE2E8F0)     // Subtle Card Border Light
val CardBorderDark = Color(0xFF334155)      // Subtle Card Border Dark

// Gradients
val PrimaryHeaderGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF0D9488))
)

val ActionGaveGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFDC2626), Color(0xFFEF4444))
)

val ActionGotGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF059669), Color(0xFF10B981))
)

// Legacy Compatibility Tokens
val GreenPrimary = EmeraldPrimary
val GreenLight = LightReceivableBg
val GreenDark = DeepEmerald
val GreenReceivable = ReceivableGreen
val RedPayable = PayableRed
val RedLight = LightPayableBg
val SettledGray = MutedSlate
val PrimaryAccent = ElectricEmerald
val SecondaryAccent = InfoBlue
