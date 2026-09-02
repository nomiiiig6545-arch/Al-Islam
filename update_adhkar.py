import re

with open("app/src/main/java/com/example/ui/screens/AdhkarScreen.kt", "r") as f:
    content = f.read()

pattern = re.compile(r"fun AdhkarCardItem.*?^}", re.MULTILINE | re.DOTALL)
replacement = """fun AdhkarCardItem(
    item: AdhkarItem,
    cardBg: Color,
    cardBorder: Color,
    accentColor: Color,
    primaryColor: Color,
    onClick: () -> Unit
) {
    val darkGreen = Color(0xFF004D40)
    val gold = Color(0xFFD4AF37)
    val lightGreen = Color(0xFFE0F2F1)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("adhkar_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        color = darkGreen,
        border = BorderStroke(1.dp, gold.copy(alpha = 0.3f)),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Number Circle Badge
                Surface(
                    shape = CircleShape,
                    color = darkGreen,
                    border = BorderStroke(1.dp, gold),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = item.number.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = gold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Transliteration/Title
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Repetition Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = darkGreen,
                    border = BorderStroke(1.dp, gold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "x${item.targetCount}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = gold
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Count with Tasbeeh",
                            tint = gold,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Arabic Text Display (RTL)
            Text(
                text = item.arabicText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = UrduNaskhFontFamily,
                    fontSize = 22.sp,
                    lineHeight = 36.sp,
                    textAlign = TextAlign.Right,
                    textDirection = TextDirection.Rtl
                ),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Benefit / Meaning caption
            Text(
                text = item.benefit,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = lightGreen
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}"""

new_content = re.sub(pattern, replacement, content)

with open("app/src/main/java/com/example/ui/screens/AdhkarScreen.kt", "w") as f:
    f.write(new_content)
