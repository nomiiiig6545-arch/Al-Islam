import re

with open('./app/src/main/java/com/example/ui/screens/TafseerScreen.kt', 'r') as f:
    content = f.read()

# 1. Remove duplicated state block
dup_block = """    var fontSizeSp by remember { mutableFloatStateOf(17f) }
    var lineHeightMultiplier by remember { mutableFloatStateOf(1.0f) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var isDarkMode by remember { mutableStateOf(false) }
    var tajweedEnabled by remember { mutableStateOf(true) }
    var autoScrollEnabled by remember { mutableStateOf(false) }
    var scriptStyle by remember { mutableStateOf("INDO_PAK") }"""

content = content.replace(dup_block, "", 1)

# 2. Find ModalBottomSheet (which is currently outside TafseerScreen)
# It starts at "    // Settings Bottom Sheet" and ends somewhere before "@Composable\nprivate fun QuickActionButton"
sheet_start = content.find("    // Settings Bottom Sheet")
sheet_end = content.find("\n@Composable\nprivate fun QuickActionButton")

if sheet_start != -1 and sheet_end != -1:
    sheet = content[sheet_start:sheet_end]
    content = content[:sheet_start] + content[sheet_end:]
    
    # We need to insert it INSIDE TafseerScreen.
    # TafseerScreen ends before the first top-level @Composable after it.
    # Actually, the AnimatedContent closes, then Box closes, then Scaffold closes, then TafseerScreen closes.
    # Let's find "        }\n    }\n}\n" which is the end of TafseerScreen.
    # We can just search for "                    2 -> TafseerReaderView" and find where that block ends.
    scaffold_end_pattern = "                    )\n                }\n            }\n        }\n    }"
    insert_pos = content.find(scaffold_end_pattern)
    if insert_pos != -1:
        # Insert inside Scaffold, right before closing box
        # wait, Box closes, then Scaffold closes.
        content = content[:insert_pos] + "                    )\n                }\n            }\n        }\n\n" + sheet + "\n    }" + content[insert_pos + len(scaffold_end_pattern):]

# Write back
with open('./app/src/main/java/com/example/ui/screens/TafseerScreen.kt', 'w') as f:
    f.write(content)
