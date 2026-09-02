const fs = require('fs');

let content = fs.readFileSync('./app/src/main/java/com/example/ui/screens/TafseerScreen.kt', 'utf8');

// 1. Remove state variables from TafseerReaderView
const statePattern = `    var fontSizeSp by remember { mutableFloatStateOf(17f) }
    var lineHeightMultiplier by remember { mutableFloatStateOf(1.0f) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    
    // Settings State
    var isDarkMode by remember { mutableStateOf(false) }
    var tajweedEnabled by remember { mutableStateOf(true) }
    var autoScrollEnabled by remember { mutableStateOf(false) }
    var scriptStyle by remember { mutableStateOf("INDO_PAK") }`;

content = content.replace(statePattern, "");

// 2. Extract ModalBottomSheet from TafseerReaderView
const sheetStartStr = `    // Settings Bottom Sheet`;
const sheetEndStr = `}\n\n/**\n * Individual Card representing an Ayah`;
const sheetStart = content.indexOf(sheetStartStr);
const sheetEnd = content.indexOf(sheetEndStr);

const sheetContent = content.substring(sheetStart, sheetEnd);

content = content.substring(0, sheetStart) + content.substring(sheetEnd);

// 3. Add ModalBottomSheet to TafseerScreen
const tafseerScreenEndStr = `        }\n    }\n}\n\n@Composable\nprivate fun QuickActionButton`;
const tafseerScreenEnd = content.indexOf(tafseerScreenEndStr);

// We'll wrap the sheet with if (showSettingsSheet) { ... } since the sheetContent already has it.
// sheetContent looks like:
//     // Settings Bottom Sheet
//     if (showSettingsSheet) { ... }
// We just insert it before the closing braces of TafseerScreen.
// Wait, tafseerScreenEndStr matches the end of the `AnimatedContent` inside `Box` inside `Scaffold` inside `TafseerScreen`.
// Let's insert it right after the `AnimatedContent` block ends. 
// Which is right before the `tafseerScreenEndStr` match `        }\n    }\n}\n\n@Composable`.

content = content.substring(0, tafseerScreenEnd) + "\n" + sheetContent + "\n" + content.substring(tafseerScreenEnd);

// 4. Update signatures and usages
content = content.replace(
    `var bookmarkedSurahs by remember { mutableStateOf(setOf(1, 18, 36, 67, 112)) }`,
    `var bookmarkedSurahs by remember { mutableStateOf(setOf(1, 18, 36, 67, 112)) }

    // Hoisted Settings State
    var fontSizeSp by remember { mutableFloatStateOf(17f) }
    var lineHeightMultiplier by remember { mutableFloatStateOf(1.0f) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var isDarkMode by remember { mutableStateOf(false) }
    var tajweedEnabled by remember { mutableStateOf(true) }
    var autoScrollEnabled by remember { mutableStateOf(false) }
    var scriptStyle by remember { mutableStateOf("INDO_PAK") }`
);

content = content.replace(
    `fun TafseerSurahIndexView(
    surahs: List<SurahTafseerItem>,`,
    `fun TafseerSurahIndexView(
    isDarkMode: Boolean,
    surahs: List<SurahTafseerItem>,`
);

content = content.replace(
    `1 -> TafseerSurahIndexView(
                        surahs = filteredSurahs,`,
    `1 -> TafseerSurahIndexView(
                        isDarkMode = isDarkMode,
                        surahs = filteredSurahs,`
);

content = content.replace(
    `fun TafseerReaderView(
    surahNumber: Int,`,
    `fun TafseerReaderView(
    surahNumber: Int,
    fontSizeSp: Float,
    lineHeightMultiplier: Float,
    onOpenSettings: () -> Unit,`
);

content = content.replace(
    `2 -> TafseerReaderView(
                        surahNumber = selectedSurahNumber,`,
    `2 -> TafseerReaderView(
                        surahNumber = selectedSurahNumber,
                        fontSizeSp = fontSizeSp,
                        lineHeightMultiplier = lineHeightMultiplier,
                        onOpenSettings = { showSettingsSheet = true },`
);

content = content.replace(
    `onClick = { showSettingsSheet = true },`,
    `onClick = onOpenSettings,`
);

// 5. Update TafseerScreen UI to use isDarkMode for currentMode == 1
content = content.replace(
    `color = if (currentMode == 0) MaterialTheme.colorScheme.surface.copy(alpha = 0.95f) else if (currentMode == 1) LightSurface else BrandGreenDark,
                shadowElevation = if (currentMode == 1) 0.dp else 4.dp,
                border = if (currentMode == 1) BorderStroke(1.dp, LightSurfaceBorder) else BorderStroke(0.5.dp, OutlineVariant.copy(alpha = 0.3f))`,
    `color = if (currentMode == 0) MaterialTheme.colorScheme.surface.copy(alpha = 0.95f) else if (currentMode == 1) (if (isDarkMode) DarkBg else LightSurface) else BrandGreenDark,
                shadowElevation = if (currentMode == 1) 0.dp else 4.dp,
                border = if (currentMode == 1) BorderStroke(1.dp, if (isDarkMode) OutlineVariant else LightSurfaceBorder) else BorderStroke(0.5.dp, OutlineVariant.copy(alpha = 0.3f))`
);

content = content.replace(
    `tint = if (currentMode == 0) BrandGreen else if (currentMode == 1) InkBlack else Color.White`,
    `tint = if (currentMode == 0) BrandGreen else if (currentMode == 1) (if (isDarkMode) Color.White else InkBlack) else Color.White`
);

content = content.replace(
    `color = if (currentMode == 0) BrandGreen else if (currentMode == 1) InkBlack else BrandYellow,`,
    `color = if (currentMode == 0) BrandGreen else if (currentMode == 1) (if (isDarkMode) Color.White else InkBlack) else BrandYellow,`
);

content = content.replace(
    `onClick = { /* TODO settings */ }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = InkBlack
                            )`,
    `onClick = { showSettingsSheet = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = if (isDarkMode) Color.White else InkBlack
                            )`
);

content = content.replace(
    `containerColor = if (currentMode == 1) LightSurface else DarkBg`,
    `containerColor = if (currentMode == 1) (if (isDarkMode) DarkBg else LightSurface) else DarkBg`
);

// 6. Update TafseerSurahIndexView to use isDarkMode
content = content.replace(
    `.background(LightSurface)`,
    `.background(if (isDarkMode) DarkBg else LightSurface)`
);

content = content.replace(
    `color = LightSurfaceCard,
                    border = BorderStroke(1.dp, LightSurfaceBorder),`,
    `color = if (isDarkMode) BrandGreenDark else LightSurfaceCard,
                    border = BorderStroke(1.dp, if (isDarkMode) OutlineVariant else LightSurfaceBorder),`
);

content = content.replace(
    `color = InkBlack,`,
    `color = if (isDarkMode) Color.White else InkBlack,`
);

content = content.replace(
    `color = BrandGreenDark,`,
    `color = if (isDarkMode) BrandYellow else BrandGreenDark,`
);

fs.writeFileSync('./app/src/main/java/com/example/ui/screens/TafseerScreen.kt', content);
