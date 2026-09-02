def fix_file(filename):
    with open(filename, "r") as f:
        content = f.read()

    # Remove duplicate isDark
    content = content.replace("    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f\n    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f", "    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f")

    # Replace activeCardBg with MaterialTheme.colorScheme.primaryContainer to fix unresolved references in sub-components
    content = content.replace("activeCardBg", "MaterialTheme.colorScheme.primaryContainer")

    with open(filename, "w") as f:
        f.write(content)

fix_file("app/src/main/java/com/example/ui/screens/ReciterSurahListScreen.kt")
