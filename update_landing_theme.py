import re

with open('./app/src/main/java/com/example/ui/screens/LandingScreen.kt', 'r') as f:
    text = f.read()

# Remove the isDarkTheme parameter from LandingCard and LandingCardWithCompoundIcon
# Remove `isDarkTheme = isDarkTheme` from usages in LandingScreen
# Replace hardcoded colors with MaterialTheme colors in LandingScreen

text = re.sub(r'isDarkTheme: Boolean = false,', '', text)
text = re.sub(r'isDarkTheme = isDarkTheme,?\n?\s*', '', text)
text = re.sub(r'val isDarkTheme = MaterialTheme\.colorScheme\.background\.luminance\(\) < 0\.5f', '', text)

text = re.sub(r'val cardBg = if \(isDarkTheme\) Color\(0xFF1E1E1E\) else MaterialTheme\.colorScheme\.primaryContainer', 'val cardBg = MaterialTheme.colorScheme.primaryContainer', text)
text = re.sub(r'val cardBorder = if \(isDarkTheme\) androidx\.compose\.foundation\.BorderStroke\(1\.dp, Color\(0xFF374151\)\) else null', 'val cardBorder = null', text)
text = re.sub(r'val iconTint = if \(isDarkTheme\) Color\(0xFFFDE047\) else MaterialTheme\.colorScheme\.secondary', 'val iconTint = MaterialTheme.colorScheme.secondary', text)
text = re.sub(r'val titleColor = Color\.White', 'val titleColor = MaterialTheme.colorScheme.onPrimaryContainer', text)
text = re.sub(r'val subtitleColor = if \(isDarkTheme\) Color\(0xFF9CA3AF\) else Color\.White\.copy\(alpha = 0\.8f\)', 'val subtitleColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)', text)

text = re.sub(r'color = if \(isDarkTheme\) Color\(0xFFFDE047\) else MaterialTheme\.colorScheme\.primary', 'color = MaterialTheme.colorScheme.primary', text)
text = re.sub(r'background\(if \(isDarkTheme\) Color\(0xFFFDE047\)\.copy\(alpha = 0\.3f\) else MaterialTheme\.colorScheme\.primary\.copy\(alpha = 0\.3f\)\)', 'background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))', text)

with open('./app/src/main/java/com/example/ui/screens/LandingScreen.kt', 'w') as f:
    f.write(text)

