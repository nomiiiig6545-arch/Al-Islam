def fix_file(filename):
    with open(filename, "r") as f:
        content = f.read()

    # Red/Error color
    content = content.replace("Color(0xFFEF4444)", "MaterialTheme.colorScheme.error")
    # Success/Light green color
    content = content.replace("Color(0xFFBAEED9)", "MaterialTheme.colorScheme.primary") 

    with open(filename, "w") as f:
        f.write(content)

fix_file("app/src/main/java/com/example/ui/screens/ReciterDownloadedSurahsScreen.kt")
