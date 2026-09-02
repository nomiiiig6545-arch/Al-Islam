import re

with open('./app/src/main/java/com/example/ui/screens/TafseerScreen.kt', 'r') as f:
    text = f.read()

replacements = {
    r'\bDarkBg\b': 'MaterialTheme.colorScheme.background',
    r'\bBrandGreen\b': 'MaterialTheme.colorScheme.primary',
    r'\bBrandGreenDark\b': 'MaterialTheme.colorScheme.primaryContainer',
    r'\bBrandYellow\b': 'MaterialTheme.colorScheme.secondary',
    r'\bAyahGold\b': 'MaterialTheme.colorScheme.secondary',
    r'\bLightSurface\b': 'MaterialTheme.colorScheme.surface',
    r'\bLightSurfaceCard\b': 'MaterialTheme.colorScheme.surfaceVariant',
    r'\bLightSurfaceBorder\b': 'MaterialTheme.colorScheme.outlineVariant',
    r'\bLightSurfaceContainer\b': 'MaterialTheme.colorScheme.surfaceVariant',
    r'\bLightSurfaceContainerLow\b': 'MaterialTheme.colorScheme.surface',
    r'\bLightTextPrimary\b': 'MaterialTheme.colorScheme.onSurface',
    r'\bLightTextMuted\b': 'MaterialTheme.colorScheme.onSurfaceVariant',
    r'\bInkBlack\b': 'MaterialTheme.colorScheme.onSurface',
    r'\bOutlineVariant\b': 'MaterialTheme.colorScheme.outlineVariant',
    r'\bTextWhite70\b': 'MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)'
}

for k, v in replacements.items():
    text = re.sub(k, v, text)

with open('./app/src/main/java/com/example/ui/screens/TafseerScreen.kt', 'w') as f:
    f.write(text)

