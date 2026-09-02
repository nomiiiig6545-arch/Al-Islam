def fix_file(filename):
    with open(filename, "r") as f:
        lines = f.readlines()

    new_lines = []
    isDark_count = 0
    for line in lines:
        if "val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f" in line:
            isDark_count += 1
            if isDark_count > 1:
                continue
        if "val MaterialTheme.colorScheme.primaryContainer = MaterialTheme.colorScheme.primaryContainer" in line:
            continue
        new_lines.append(line)

    with open(filename, "w") as f:
        f.writelines(new_lines)

fix_file("app/src/main/java/com/example/ui/screens/ReciterDownloadedSurahsScreen.kt")
fix_file("app/src/main/java/com/example/ui/screens/ReciterSurahListScreen.kt")
