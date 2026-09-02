def fix_file(filename):
    with open(filename, "r") as f:
        content = f.read()

    # Replace activeCardBg with MaterialTheme.colorScheme.primaryContainer
    content = content.replace("activeCardBg", "MaterialTheme.colorScheme.primaryContainer")
    
    # Also remove duplicated isDark if any
    content = content.replace("    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f\n    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f", "    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f")

    with open(filename, "w") as f:
        f.write(content)

fix_file("app/src/main/java/com/example/ui/screens/ReciterDownloadedSurahsScreen.kt")
