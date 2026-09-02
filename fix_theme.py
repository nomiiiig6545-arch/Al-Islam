import re

def fix_file(filename):
    with open(filename, "r") as f:
        content = f.read()

    # Replace hardcoded theme tokens
    var_replacement = """    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Dynamic Theme Tokens
    val containerBg = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surface
    val cardBorder = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val primaryGreen = MaterialTheme.colorScheme.primary
    val goldYellow = MaterialTheme.colorScheme.secondary
    val activeCardBg = MaterialTheme.colorScheme.primaryContainer"""

    # For ReciterDownloadedSurahsScreen
    content = re.sub(r"    val isDark = true.*?val activeCardBg = Color\(0xFF0F5233\)", var_replacement, content, flags=re.DOTALL)
    # For ReciterSurahListScreen
    content = re.sub(r"    // Dark Emerald Theme Tokens.*?val activeCardBg = Color\(0xFF0F5233\)", var_replacement, content, flags=re.DOTALL)

    # In both files, there are hardcoded text colors.
    # Replace Color.White with MaterialTheme.colorScheme.onSurface (or onPrimary if in a primary button, but let's carefully check)
    
    # We will let a more targeted replace happen. Let's see the context of Color.White in ReciterSurahListScreen.
    
    with open(filename, "w") as f:
        f.write(content)

fix_file("app/src/main/java/com/example/ui/screens/ReciterDownloadedSurahsScreen.kt")
fix_file("app/src/main/java/com/example/ui/screens/ReciterSurahListScreen.kt")
