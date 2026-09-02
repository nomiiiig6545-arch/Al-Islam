#!/bin/bash
sed -i 's/Canvas(modifier = Modifier.fillMaxSize()) {/val primaryColor = MaterialTheme.colorScheme.primary\n                Canvas(modifier = Modifier.fillMaxSize()) {/g' app/src/main/java/com/example/ui/screens/QiblaScreen.kt
sed -i 's/color = MaterialTheme.colorScheme.primary,/color = primaryColor,/g' app/src/main/java/com/example/ui/screens/QiblaScreen.kt
