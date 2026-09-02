import re

with open("app/src/main/java/com/example/ui/screens/TasbeehCounterScreen.kt", "r") as f:
    content = f.read()

# Replace variables at the beginning of TasbeehCounterScreen
var_replacement = """    val darkGreen = Color(0xFF004D40)
    val gold = Color(0xFFD4AF37)
    val lightGreen = Color(0xFFE0F2F1)
    val dark = Color(0xFF111111)
    val cardBg = darkGreen
    val cardBorder = gold.copy(alpha = 0.5f)
    val displayBg = dark
    val displayBorder = gold
    val buttonBg = gold
    val buttonText = dark"""

content = re.sub(r"    val isDark = MaterialTheme.colorScheme.background.luminance\(\) < 0.5f.*?    val displayBg = MaterialTheme.colorScheme.surface", var_replacement, content, flags=re.DOTALL)

# TopAppBar background and text
content = re.sub(r"TopAppBarDefaults.topAppBarColors\(\s*containerColor = MaterialTheme.colorScheme.background\s*\)", "TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)", content)
content = re.sub(r"color = MaterialTheme.colorScheme.onSurface", "color = MaterialTheme.colorScheme.onSurface", content) # leave as is

# Main container color
# Scaffold containerColor = MaterialTheme.colorScheme.background
content = re.sub(r"Scaffold\(\s*topBar = \{", "Scaffold(\n        containerColor = MaterialTheme.colorScheme.background,\n        topBar = {", content)

# Change Text(text = "Tasbeeh", ...) to use darkGreen and big size
content = re.sub(r"Text\(\s*text = \"Tasbeeh\".*?style = MaterialTheme.typography.displayMedium.*?color = primaryColor\s*\)", """Text(
                        text = "Tasbeeh",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = HandmadeBrushesFontFamily,
                            fontSize = 48.sp,
                            letterSpacing = 1.sp,
                            color = darkGreen
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )""", content, flags=re.DOTALL)

# Change Text(text = "Count your Dhikr") to use gold
content = re.sub(r"Text\(\s*text = \"Count your Dhikr\".*?color = accentColor\s*\)", """Text(
                        text = "Unlimited Hasanaat",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = gold,
                            fontFamily = HandmadeBrushesFontFamily,
                            fontSize = 24.sp
                        )
                    )""", content, flags=re.DOTALL)

# Change Surface for Tasbeeh Card to use darkGreen
content = re.sub(r"Surface\(\s*modifier = Modifier\s*\.fillMaxWidth\(\)\s*\.weight\(1f\).*?shape = RoundedCornerShape\(24\.dp\),\s*color = cardBg,\s*border = BorderStroke\(1\.dp, cardBorder\)", """Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = darkGreen,
                    border = BorderStroke(1.dp, cardBorder)""", content, flags=re.DOTALL)

# Change arabicText color to white
content = re.sub(r"color = MaterialTheme.colorScheme.onSurface", "color = Color.White", content)
content = re.sub(r"color = MaterialTheme.colorScheme.onSurfaceVariant", "color = lightGreen", content)

# Change Display Surface to use dark
content = re.sub(r"color = displayBg,\s*border = BorderStroke\(2\.5\.dp, accentColor\)", "color = dark, border = BorderStroke(2.5.dp, dark)", content)
content = re.sub(r"color = accentColor", "color = gold", content) # for counter text and remaining
content = re.sub(r"containerColor = accentColor,\s*contentColor = MaterialTheme.colorScheme.onSecondary", "containerColor = gold, contentColor = dark", content)
content = re.sub(r"border = BorderStroke\(1\.dp, accentColor\),\s*colors = ButtonDefaults.outlinedButtonColors\(\s*contentColor = accentColor\s*\)", "border = BorderStroke(1.dp, gold), colors = ButtonDefaults.outlinedButtonColors(contentColor = gold, containerColor = dark)", content)
content = re.sub(r"tint = accentColor", "tint = gold", content)


with open("app/src/main/java/com/example/ui/screens/TasbeehCounterScreen.kt", "w") as f:
    f.write(content)
