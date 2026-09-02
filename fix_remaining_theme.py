import re

def fix_file(filename):
    with open(filename, "r") as f:
        content = f.read()

    # Generic replacements for hardcoded colors
    content = content.replace("Color(0xFF0F5233)", "activeCardBg")
    content = content.replace("Color(0xFF122E22)", "MaterialTheme.colorScheme.surfaceVariant") 

    with open(filename, "w") as f:
        f.write(content)

fix_file("app/src/main/java/com/example/ui/screens/ReciterDownloadedSurahsScreen.kt")
fix_file("app/src/main/java/com/example/ui/screens/ReciterSurahListScreen.kt")
