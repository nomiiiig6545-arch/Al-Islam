import re

with open("app/src/main/java/com/example/ui/screens/TasbeehCounterScreen.kt", "r") as f:
    content = f.read()

# find exactly the second containerColor and remove it
content = re.sub(r"        containerColor = MaterialTheme.colorScheme.background\n    } \{ innerPadding ->", "    } { innerPadding ->", content)

# I also noticed color = Color.White for Al-Quran Majeed header, but the screenshot has a white background for the header and Black text for "Al-Quran Majeed" in light mode... Wait, if the Scaffold is background color, we should use onSurface for Al-Quran Majeed. 
content = re.sub(r"color = Color.White", "color = MaterialTheme.colorScheme.onSurface", content)

with open("app/src/main/java/com/example/ui/screens/TasbeehCounterScreen.kt", "w") as f:
    f.write(content)
