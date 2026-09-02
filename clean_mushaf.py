import re

with open('app/src/main/java/com/example/ui/screens/MushafScreen.kt', 'r') as f:
    content = f.read()

# Remove showOverlaySheet bottom sheet
content = re.sub(r'if \(showOverlaySheet\) \{.*?(?=if \(showJumpSheet\))', '', content, flags=re.DOTALL)

# Remove overlaySetting variable and imports
content = re.sub(r'var overlaySetting by remember.*?\}', '', content, flags=re.DOTALL)
content = re.sub(r'var showOverlaySheet by remember.*?\}', '', content, flags=re.DOTALL)

# Remove references to overlaySetting in function calls
content = re.sub(r'overlaySetting = overlaySetting,', '', content)
content = re.sub(r'overlaySetting: PageOverlaySetting,', '', content)
content = re.sub(r'val textColor = MaterialTheme.colorScheme.onBackground', '', content)
content = re.sub(r'val headerColor = MaterialTheme.colorScheme.primary', '', content)
content = re.sub(r'color = headerColor', 'color = MaterialTheme.colorScheme.primary', content)
content = re.sub(r'color = textColor', 'color = MaterialTheme.colorScheme.onBackground', content)

with open('app/src/main/java/com/example/ui/screens/MushafScreen.kt', 'w') as f:
    f.write(content)
