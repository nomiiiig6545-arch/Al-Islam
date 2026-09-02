with open("app/src/main/java/com/example/ui/screens/TasbeehCounterScreen.kt", "r") as f:
    content = f.read()

content = content.replace("border = BorderStroke(2.5.dp, dark)", "border = BorderStroke(2.5.dp, accentColor)")
content = content.replace("contentColor = dark", "contentColor = MaterialTheme.colorScheme.onSecondary")
content = content.replace("containerColor = dark", "containerColor = Color.Transparent")

with open("app/src/main/java/com/example/ui/screens/TasbeehCounterScreen.kt", "w") as f:
    f.write(content)
