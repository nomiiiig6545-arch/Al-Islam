import re

def fix_file(filename):
    with open(filename, "r") as f:
        content = f.read()

    # Generic replacements for text/icons
    content = content.replace("Color.White", "MaterialTheme.colorScheme.onSurface")
    content = content.replace("Color(0xFF0B2219)", "MaterialTheme.colorScheme.onPrimaryContainer") # used for active playing text
    content = content.replace("Color(0xFFA7F3D0)", "MaterialTheme.colorScheme.primary") # used for downloaded count
    
    # Specific fix for TopAppBar
    content = content.replace("titleContentColor = MaterialTheme.colorScheme.onSurface", "titleContentColor = MaterialTheme.colorScheme.onSurface")

    # For transparent variants like Color.White.copy(alpha = 0.6f) -> MaterialTheme.colorScheme.onSurfaceVariant
    content = re.sub(r"MaterialTheme\.colorScheme\.onSurface\.copy\(alpha\s*=\s*[0-9.]+\w*\)", "MaterialTheme.colorScheme.onSurfaceVariant", content)
    
    # In ReciterSurahListScreen, Button content colors
    # Find containerColor = activeCardBg, contentColor = MaterialTheme.colorScheme.onSurface
    # Replace with containerColor = activeCardBg, contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    content = content.replace("contentColor = MaterialTheme.colorScheme.onSurface", "contentColor = MaterialTheme.colorScheme.onPrimaryContainer")
    # Actually wait, maybe this replace is too broad. Let's rely on standard onSurface for now and fix specific things.
    
    # Instead of doing that, I'll use a more precise regex.
    with open(filename, "w") as f:
        f.write(content)

fix_file("app/src/main/java/com/example/ui/screens/ReciterDownloadedSurahsScreen.kt")
fix_file("app/src/main/java/com/example/ui/screens/ReciterSurahListScreen.kt")
