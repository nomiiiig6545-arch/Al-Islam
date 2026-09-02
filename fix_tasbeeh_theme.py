import re

with open("app/src/main/java/com/example/ui/screens/TasbeehCounterScreen.kt", "r") as f:
    content = f.read()

# Replace hardcoded variables with theme colors
var_replacement = """    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primaryColor = MaterialTheme.colorScheme.primary
    val accentColor = MaterialTheme.colorScheme.secondary
    val cardBg = MaterialTheme.colorScheme.surfaceVariant
    val cardBorder = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val displayBg = MaterialTheme.colorScheme.surface"""

content = re.sub(r"    val darkGreen = Color\(0xFF004D40\).*?val buttonText = dark", var_replacement, content, flags=re.DOTALL)

# Revert specific color overrides
content = content.replace('color = Color.White', 'color = MaterialTheme.colorScheme.onSurface')
content = content.replace('color = darkGreen', 'color = primaryColor')
content = content.replace('color = gold', 'color = accentColor')
content = content.replace('tint = gold', 'tint = accentColor')
content = content.replace('color = dark', 'color = displayBg')
content = content.replace('color = lightGreen', 'color = MaterialTheme.colorScheme.onSurfaceVariant')
content = content.replace('color = displayBg, border = BorderStroke(2.5.dp, displayBg)', 'color = displayBg, border = BorderStroke(2.5.dp, accentColor)')
content = content.replace('color = cardBg,\n                    border = BorderStroke(1.dp, cardBorder)', 'color = cardBg,\n                    border = BorderStroke(1.dp, cardBorder)')
content = content.replace('containerColor = accentColor, contentColor = displayBg', 'containerColor = accentColor, contentColor = MaterialTheme.colorScheme.onSecondary')
content = content.replace('colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor, containerColor = displayBg)', 'colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor)')
content = content.replace('tint = if (isVibrationEnabled) accentColor else MaterialTheme.colorScheme.onSurfaceVariant', 'tint = if (isVibrationEnabled) accentColor else MaterialTheme.colorScheme.onSurfaceVariant')

with open("app/src/main/java/com/example/ui/screens/TasbeehCounterScreen.kt", "w") as f:
    f.write(content)
