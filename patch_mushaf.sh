#!/bin/bash
sed -i 's/Color(0xFF1B5E20)/MaterialTheme.colorScheme.primary/g' app/src/main/java/com/example/ui/screens/MushafScreen.kt
sed -i 's/Color(0xFF1E1E1E)/MaterialTheme.colorScheme.surfaceVariant/g' app/src/main/java/com/example/ui/screens/MushafScreen.kt
sed -i 's/Color(0xFF121212)/MaterialTheme.colorScheme.background/g' app/src/main/java/com/example/ui/screens/MushafScreen.kt
sed -i 's/Color(0xFFE8F5E9)/MaterialTheme.colorScheme.primaryContainer/g' app/src/main/java/com/example/ui/screens/MushafScreen.kt
sed -i 's/Color(0xFF2E3B2B)/MaterialTheme.colorScheme.onPrimaryContainer/g' app/src/main/java/com/example/ui/screens/MushafScreen.kt
sed -i 's/Color.White/MaterialTheme.colorScheme.surface/g' app/src/main/java/com/example/ui/screens/MushafScreen.kt
sed -i 's/Color.LightGray/MaterialTheme.colorScheme.onSurfaceVariant/g' app/src/main/java/com/example/ui/screens/MushafScreen.kt
sed -i 's/Color.Gray/MaterialTheme.colorScheme.onSurfaceVariant/g' app/src/main/java/com/example/ui/screens/MushafScreen.kt
sed -i 's/Color(0xFFFFD54F)/MaterialTheme.colorScheme.secondary/g' app/src/main/java/com/example/ui/screens/MushafScreen.kt
sed -i 's/Color(0xFF2E7D32)/MaterialTheme.colorScheme.primary/g' app/src/main/java/com/example/ui/screens/MushafScreen.kt
sed -i 's/val textColor = overlaySetting.getTextColor()/val textColor = MaterialTheme.colorScheme.onBackground/g' app/src/main/java/com/example/ui/screens/MushafScreen.kt
sed -i 's/val headerColor = overlaySetting.getHeaderColor()/val headerColor = MaterialTheme.colorScheme.primary/g' app/src/main/java/com/example/ui/screens/MushafScreen.kt
sed -i 's/containerColor = overlaySetting.getBackgroundColor()/containerColor = MaterialTheme.colorScheme.background/g' app/src/main/java/com/example/ui/screens/MushafScreen.kt
