#!/bin/bash
# We'll just replace the icon with an empty Spacer and remove the ModalBottomSheet
sed -i 's/IconButton(onClick = { showOverlaySheet = true }) {/Spacer(modifier = Modifier.width(48.dp)) \/\/ /g' app/src/main/java/com/example/ui/screens/MushafScreen.kt

# Let's remove the ModalBottomSheet block. It's too big for sed, let's use python or perl.
