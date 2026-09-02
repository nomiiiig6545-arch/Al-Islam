#!/bin/bash
sed -i 's/val greenHeaderColor = Color(0xFF2E7D32)/val greenHeaderColor = MaterialTheme.colorScheme.primary/g' app/src/main/java/com/example/ui/screens/PrayerTimesScreen.kt
sed -i 's/val darkGreenHeaderColor = Color(0xFF1B5E20)/val darkGreenHeaderColor = MaterialTheme.colorScheme.primaryContainer/g' app/src/main/java/com/example/ui/screens/PrayerTimesScreen.kt
sed -i 's/val goldCardColor = Color(0xFFD4A343)/val goldCardColor = MaterialTheme.colorScheme.secondary/g' app/src/main/java/com/example/ui/screens/PrayerTimesScreen.kt
sed -i 's/Color(0xFFF7F9F7)/MaterialTheme.colorScheme.background/g' app/src/main/java/com/example/ui/screens/PrayerTimesScreen.kt
sed -i 's/Color(0xFFE0E0E0)/MaterialTheme.colorScheme.outline/g' app/src/main/java/com/example/ui/screens/PrayerTimesScreen.kt
sed -i 's/Color(0xFFEEEEEE)/MaterialTheme.colorScheme.outlineVariant/g' app/src/main/java/com/example/ui/screens/PrayerTimesScreen.kt
sed -i 's/val activeBorderColor = Color(0xFF81C784)/val activeBorderColor = MaterialTheme.colorScheme.primary/g' app/src/main/java/com/example/ui/screens/PrayerTimesScreen.kt
sed -i 's/val activeBackgroundColor = Color(0xFFE8F5E9)/val activeBackgroundColor = MaterialTheme.colorScheme.primaryContainer/g' app/src/main/java/com/example/ui/screens/PrayerTimesScreen.kt
sed -i 's/Color(0xFF2E7D32)/MaterialTheme.colorScheme.primary/g' app/src/main/java/com/example/ui/screens/PrayerTimesScreen.kt
sed -i 's/Color(0xFF212121)/MaterialTheme.colorScheme.onSurface/g' app/src/main/java/com/example/ui/screens/PrayerTimesScreen.kt
sed -i 's/Color(0xFF388E3C)/MaterialTheme.colorScheme.primary/g' app/src/main/java/com/example/ui/screens/PrayerTimesScreen.kt
sed -i 's/Color(0xFF757575)/MaterialTheme.colorScheme.onSurfaceVariant/g' app/src/main/java/com/example/ui/screens/PrayerTimesScreen.kt
