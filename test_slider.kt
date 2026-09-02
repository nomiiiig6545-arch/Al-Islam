import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestSlider() {
    Slider(
        value = 0.5f,
        onValueChange = {},
        thumb = {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color.White, CircleShape)
            )
        }
    )
}
