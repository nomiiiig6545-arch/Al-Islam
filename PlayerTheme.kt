package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class PlayerThemeId(
    val id: String,
    val title: String,
    val titleUrdu: String,
    val topGradientHex: Long,
    val bottomGradientHex: Long,
    val accentPrimaryHex: Long,
    val accentSecondaryHex: Long,
    val cardBgHex: Long,
    val isLight: Boolean = false
) {
    MIDNIGHT_DARK(
        id = "MIDNIGHT_DARK",
        title = "Midnight Charcoal",
        titleUrdu = "ڈارک موڈ",
        topGradientHex = 0xFF353546,
        bottomGradientHex = 0xFF14141C,
        accentPrimaryHex = 0xFFF59E0B, // Amber Gold
        accentSecondaryHex = 0xFF6EE7B7, // Soft Mint
        cardBgHex = 0xFF242432,
        isLight = false
    ),
    EMERALD_GREEN(
        id = "EMERALD_GREEN",
        title = "Islamic Emerald",
        titleUrdu = "اسلامک زمردی",
        topGradientHex = 0xFF0D472B,
        bottomGradientHex = 0xFF052014,
        accentPrimaryHex = 0xFFF7C325, // Rich Gold
        accentSecondaryHex = 0xFF10B981, // Vivid Emerald
        cardBgHex = 0xFF0F3924,
        isLight = false
    ),
    ROYAL_GOLD(
        id = "ROYAL_GOLD",
        title = "Royal Amber & Gold",
        titleUrdu = "شاہی سنہری",
        topGradientHex = 0xFF4A3416,
        bottomGradientHex = 0xFF1C1305,
        accentPrimaryHex = 0xFFFBBF24, // Bright Gold
        accentSecondaryHex = 0xFFF97316, // Warm Amber
        cardBgHex = 0xFF34240F,
        isLight = false
    ),
    OCEAN_BLUE(
        id = "OCEAN_BLUE",
        title = "Ocean Sapphire",
        titleUrdu = "نیلم سمندر",
        topGradientHex = 0xFF132D4E,
        bottomGradientHex = 0xFF071220,
        accentPrimaryHex = 0xFF38BDF8, // Luminous Cyan
        accentSecondaryHex = 0xFF818CF8, // Indigo Blue
        cardBgHex = 0xFF10233D,
        isLight = false
    ),
    MYSTIC_PURPLE(
        id = "MYSTIC_PURPLE",
        title = "Mystic Amethyst",
        titleUrdu = "یاقوتی وائلٹ",
        topGradientHex = 0xFF3A184A,
        bottomGradientHex = 0xFF170820,
        accentPrimaryHex = 0xFFC084FC, // Lavender Purple
        accentSecondaryHex = 0xFFF472B6, // Rose Gold
        cardBgHex = 0xFF2C1338,
        isLight = false
    ),
    SUNSET_ROSE(
        id = "SUNSET_ROSE",
        title = "Sunset Crimson",
        titleUrdu = "غروبِ گلابی",
        topGradientHex = 0xFF4A1726,
        bottomGradientHex = 0xFF1F070E,
        accentPrimaryHex = 0xFFFB7185, // Rose Pink
        accentSecondaryHex = 0xFFFDA4AF,
        cardBgHex = 0xFF36101B,
        isLight = false
    ),
    CHARCOAL_SLATE(
        id = "CHARCOAL_SLATE",
        title = "Minimal Slate",
        titleUrdu = "سلیٹ سرمئی",
        topGradientHex = 0xFF222933,
        bottomGradientHex = 0xFF0E1116,
        accentPrimaryHex = 0xFF34D399, // Emerald Mint
        accentSecondaryHex = 0xFF94A3B8, // Silver Steel
        cardBgHex = 0xFF181C23,
        isLight = false
    ),
    PURE_LIGHT(
        id = "PURE_LIGHT",
        title = "Ivory Elegance (Light)",
        titleUrdu = "روشن موتی (Light)",
        topGradientHex = 0xFFF8FAFC,
        bottomGradientHex = 0xFFE2E8F0,
        accentPrimaryHex = 0xFF0D6847, // Deep Emerald Green
        accentSecondaryHex = 0xFFD97706, // Amber Gold
        cardBgHex = 0xFFFFFFFF,
        isLight = true
    );

    val topGradientColor: Color get() = Color(topGradientHex)
    val bottomGradientColor: Color get() = Color(bottomGradientHex)
    val accentPrimary: Color get() = Color(accentPrimaryHex)
    val accentSecondary: Color get() = Color(accentSecondaryHex)
    val cardBg: Color get() = Color(cardBgHex)
    val textPrimary: Color get() = if (isLight) Color(0xFF0F172A) else Color.White
    val textSecondary: Color get() = if (isLight) Color(0xFF475569) else Color.White.copy(alpha = 0.7f)
    val controlTint: Color get() = if (isLight) Color(0xFF0F172A) else Color.White
    val inactiveTint: Color get() = if (isLight) Color(0xFF94A3B8) else Color.White.copy(alpha = 0.45f)
    val playButtonBg: Color get() = if (isLight) Color(0xFF0D6847) else Color.White
    val playButtonIcon: Color get() = if (isLight) Color.White else Color(0xFF14141C)

    companion object {
        fun fromId(id: String?): PlayerThemeId {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MIDNIGHT_DARK
        }
    }
}
