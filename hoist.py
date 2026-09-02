import re

with open('./app/src/main/java/com/example/ui/screens/TafseerScreen.kt', 'r') as f:
    content = f.read()

# 1. Remove state variables from TafseerReaderView (lines ~562-572)
state_pattern = r"    var fontSizeSp by remember \{ mutableFloatStateOf\(17f\) \}\n    var lineHeightMultiplier by remember \{ mutableFloatStateOf\(1.0f\) \}\n    var showSettingsSheet by remember \{ mutableStateOf\(false\) \}\n    \n    // Settings State\n    var isDarkMode by remember \{ mutableStateOf\(false\) \}\n    var tajweedEnabled by remember \{ mutableStateOf\(true\) \}\n    var autoScrollEnabled by remember \{ mutableStateOf\(false\) \}\n    var scriptStyle by remember \{ mutableStateOf\(\"INDO_PAK\"\) \}\n"
content = re.sub(state_pattern, "", content)

# 2. Extract ModalBottomSheet from TafseerReaderView
sheet_start = content.find("    // Settings Bottom Sheet")
sheet_end = content.find("}\n\n/**\n * Individual Card representing an Ayah")
sheet_content = content[sheet_start:sheet_end]

content = content[:sheet_start] + content[sheet_end:]

# 3. Add ModalBottomSheet to TafseerScreen (after the Scaffold)
scaffold_end = content.find("    Scaffold(")
# let's find the closing brace of TafseerScreen by finding where `TafseerSurahIndexView` starts
tafseer_screen_end = content.find("\n@Composable\nprivate fun QuickActionButton")

# We'll inject the sheet_content right before tafseer_screen_end, but wait, the sheet_content has an extra level of indentation. We can just inject it.
# Actually, wait, TafseerScreen ends before QuickActionButton. Let's find the exact end of TafseerScreen.
end_of_tafseer_screen = content.find("}\n\n@Composable\nprivate fun QuickActionButton")

if sheet_content not in content[:end_of_tafseer_screen]:
    content = content[:end_of_tafseer_screen] + "\n\n" + sheet_content + "\n" + content[end_of_tafseer_screen:]

# 4. Update signatures and usages
# For TafseerSurahIndexView
content = content.replace(
    "fun TafseerSurahIndexView(\n    isDarkMode: Boolean,\n    surahs:",
    "fun TafseerSurahIndexView(\n    isDarkMode: Boolean,\n    surahs:"
)
content = content.replace(
    "1 -> TafseerSurahIndexView(\n                        surahs = filteredSurahs,",
    "1 -> TafseerSurahIndexView(\n                        isDarkMode = isDarkMode,\n                        surahs = filteredSurahs,"
)

# For TafseerReaderView signature
content = content.replace(
    "fun TafseerReaderView(\n    surahNumber: Int,",
    "fun TafseerReaderView(\n    surahNumber: Int,\n    fontSizeSp: Float,\n    lineHeightMultiplier: Float,\n    onOpenSettings: () -> Unit,"
)

# For TafseerReaderView invocation
content = content.replace(
    "2 -> TafseerReaderView(\n                        surahNumber = selectedSurahNumber,",
    "2 -> TafseerReaderView(\n                        surahNumber = selectedSurahNumber,\n                        fontSizeSp = fontSizeSp,\n                        lineHeightMultiplier = lineHeightMultiplier,\n                        onOpenSettings = { showSettingsSheet = true },"
)

# Inside TafseerReaderView, replace `showSettingsSheet = true` with `onOpenSettings()`
content = content.replace(
    "onClick = { showSettingsSheet = true },",
    "onClick = onOpenSettings,"
)

# 5. Fix currentMode == 1 settings click
content = content.replace(
    "onClick = { /* TODO settings */ }",
    "onClick = { showSettingsSheet = true }"
)

with open('./app/src/main/java/com/example/ui/screens/TafseerScreen.kt', 'w') as f:
    f.write(content)
