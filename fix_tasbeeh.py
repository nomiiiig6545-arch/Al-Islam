import re

with open("app/src/main/java/com/example/ui/screens/TasbeehCounterScreen.kt", "r") as f:
    content = f.read()

# Replace accentColor with gold
content = re.sub(r"accentColor", "gold", content)
content = re.sub(r"Color\.WhiteVariant", "lightGreen", content)

with open("app/src/main/java/com/example/ui/screens/TasbeehCounterScreen.kt", "w") as f:
    f.write(content)
