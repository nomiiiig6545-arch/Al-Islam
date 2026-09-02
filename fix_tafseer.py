import glob
import os

files = glob.glob('app/src/main/java/com/example/data/JawahirTafseerProvider*.kt')
for filepath in files:
    filename = os.path.basename(filepath)
    classname = filename.replace('.kt', '')
    
    content = f"""package com.example.data

object {classname} {{
    val exactTafseerData = mapOf<String, String>()
    
    fun getExactText(surahNumber: Int, ayahNumber: Int): String? {{
        return exactTafseerData["${{surahNumber}}_${{ayahNumber}}"]
    }}
}}
"""
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
