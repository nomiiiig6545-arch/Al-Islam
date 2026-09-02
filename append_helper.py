import sys

def append_to_kotlin(data):
    file_path = "app/src/main/java/com/example/data/JawahirTafseerProvider.kt"
    with open(file_path, "r") as f:
        content = f.read()

    marker = "\n    )\n"
    if marker in content:
        parts = content.split(marker, 1)
        new_entries = ""
        for key, value in data.items():
            new_entries += f',\n        "{key}" to """\n{value}\n        """.trimIndent()'
        
        new_content = parts[0] + new_entries + marker + parts[1]
        
        with open(file_path, "w") as f:
            f.write(new_content)
        print(f"Successfully appended {len(data)} ayahs.")
    else:
        print("Marker not found!")
