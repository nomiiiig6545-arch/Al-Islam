package com.example.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.Reciter

/**
 * Reusable component to display a Reciter's circular photo.
 * It intelligently resolves:
 * 1. Explicit drawable resource ID (reciter.imageRes)
 * 2. Dynamically named drawable in res/drawable (e.g. "saad_al_ghamdi", "abdul_basit", etc.)
 * 3. Remote photo URL via Coil AsyncImage
 * 4. Fallback Islamic monogram avatar with initials and gold border ring
 */
@SuppressLint("DiscouragedApi")
@Composable
fun ReciterImage(
    reciter: Reciter,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    borderWidth: Dp = 2.dp,
    borderColor: Color = Color(0xFFE5A910),
    showBorder: Boolean = true
) {
    val context = LocalContext.current

    // Potential drawable names based on reciter attributes
    val candidateDrawableNames = remember(reciter.id, reciter.name) {
        val sanitized = reciter.name.lowercase()
            .replace(" ", "_")
            .replace("-", "_")
            .replace("'", "")
            .replace(".", "")
        val idClean = reciter.id.replace("ar.", "").replace("ur.", "").replace(".", "_")
        listOf(
            "img_reciter_$idClean",
            "img_reciter_sudais",
            "img_reciter_alafasy",
            "img_reciter_ghamdi",
            "img_reciter_abdulbasit",
            "img_reciter_qari",
            sanitized,
            idClean,
            "qari_$idClean",
            "reciter_$idClean",
            sanitized.replace("abdur_rahman", "abdur_rahman_al"),
            sanitized.replace("abdur_rahman", "abdur_rahman_as"),
            sanitized.replace("al_", ""),
            sanitized.replace("as_", ""),
            "abdur_rahman_as_sudais",
            "abdur_rahman_al_sudais",
            "mishary_rashid_alafasy",
            "mishari_rashid",
            "saad_al_ghamdi",
            "maher_al_muaiqly",
            "abdul_basit",
            "abdul_basit_abdul_samad",
            "ahmed_al_ajmy",
            "saud_al_shuraim",
            "mahmoud_al_husary",
            "abu_bakr_al_shatri",
            "yasser_al_dosari"
        ).distinct()
    }

    val localDrawableResId = remember(reciter.imageRes, candidateDrawableNames) {
        if (reciter.imageRes != null && reciter.imageRes != 0) {
            reciter.imageRes
        } else {
            var foundId = 0
            for (name in candidateDrawableNames) {
                val id = context.resources.getIdentifier(name, "drawable", context.packageName)
                if (id != 0) {
                    foundId = id
                    break
                }
            }
            foundId
        }
    }

    // Candidate photo URLs list for reliable loading across CDNs
    val candidatePhotoUrls = remember(reciter.id, reciter.photoUrl) {
        val list = mutableListOf<String>()
        if (!reciter.photoUrl.isNullOrBlank()) {
            list.add(reciter.photoUrl)
            if (reciter.photoUrl.contains("static.qurancdn.com")) {
                list.add(reciter.photoUrl.replace("static.qurancdn.com", "quran.com"))
                list.add(reciter.photoUrl.replace("static.qurancdn.com", "audio.qurancdn.com"))
            }
        }
        when (reciter.id) {
            "ar.sudais" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/8/8e/Abdur-Rahman_as-Sudais.jpg/440px-Abdur-Rahman_as-Sudais.jpg")
                list.add("https://server11.mp3quran.net/sds/profile.jpg")
            }
            "ar.alafasy" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/d/d7/Mishary_Rashid_Alafasy.jpg/440px-Mishary_Rashid_Alafasy.jpg")
                list.add("https://server8.mp3quran.net/afs/profile.jpg")
            }
            "ar.saadghamadi" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/a/a2/Saad_al_Ghamdi.jpg/440px-Saad_al_Ghamdi.jpg")
            }
            "ar.maher" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/3/3d/Maher_Al_Mueaqly.jpg/440px-Maher_Al_Mueaqly.jpg")
            }
            "ar.abdulbasitmurattal" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/f/f8/Abdul_Basit_Abdul_Samad.jpg/440px-Abdul_Basit_Abdul_Samad.jpg")
            }
            "ar.shuraim" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/2/22/Saud_Shuraim.jpg/440px-Saud_Shuraim.jpg")
            }
            "ar.dosari" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/9/90/Yasser_Al-Dosari.jpg/440px-Yasser_Al-Dosari.jpg")
                list.add("https://server11.mp3quran.net/yasser/profile.jpg")
            }
            "ar.alajamy" -> {
                list.add("https://server10.mp3quran.net/ajm/profile.jpg")
            }
            "ar.husary" -> {
                list.add("https://server13.mp3quran.net/husr/profile.jpg")
            }
            "ar.shaatree" -> {
                list.add("https://server11.mp3quran.net/shatri/profile.jpg")
            }
            "ar.oosi" -> {
                list.add("https://server6.mp3quran.net/aloosi/profile.jpg")
            }
            "ar.fares" -> {
                list.add("https://server8.mp3quran.net/frs_a/profile.jpg")
            }
            "ar.baleela" -> {
                list.add("https://server6.mp3quran.net/balilah/profile.jpg")
            }
            "ur.sadaqat" -> {
                list.add("https://archive.org/services/img/Al_Quran_Qari_Syed_Sadaqat_Ali")
            }
            "ur.waheed" -> {
                list.add("https://archive.org/services/img/Al_Quran-Urdu_Translation_Qari_Waheed_Zafar_Qasmi-High_Quality_201410")
            }
            "ur.khan" -> {
                list.add("https://archive.org/services/img/UrduTranslationProf.ShamshadAliKhanWithMisharyRashidAlafasy")
            }
        }
        list.distinct()
    }

    var currentUrlIndex by remember(reciter.id) { mutableIntStateOf(0) }
    val activePhotoUrl = candidatePhotoUrls.getOrNull(currentUrlIndex)

    val initials = remember(reciter.name) {
        reciter.name.split(" ")
            .filter { it.isNotBlank() && !it.equals("Ibn", ignoreCase = true) && !it.equals("Al-", ignoreCase = true) && !it.equals("As-", ignoreCase = true) }
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifEmpty { "QR" }
    }

    val avatarGradients = remember(reciter.id) {
        when ((reciter.id.hashCode() % 4 + 4) % 4) {
            0 -> listOf(Color(0xFF1B4D3E), Color(0xFF0F3026))
            1 -> listOf(Color(0xFF2C5E4E), Color(0xFF143B2E))
            2 -> listOf(Color(0xFF2E6152), Color(0xFF1B3D33))
            else -> listOf(Color(0xFF234B3D), Color(0xFF0C241C))
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (showBorder) {
                    Modifier.border(borderWidth, borderColor, CircleShape)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (localDrawableResId != 0) {
            // Render local drawable asset directly
            Image(
                painter = painterResource(id = localDrawableResId),
                contentDescription = reciter.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (!activePhotoUrl.isNullOrBlank()) {
            val imageRequest = remember(activePhotoUrl) {
                ImageRequest.Builder(context)
                    .data(activePhotoUrl)
                    .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36")
                    .addHeader("Accept", "image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
                    .crossfade(true)
                    .build()
            }

            // Render remote photo URL via Coil with User-Agent & fallback
            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = reciter.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    FallbackMonogram(avatarGradients, initials, borderColor, size)
                },
                error = {
                    LaunchedEffect(activePhotoUrl) {
                        if (currentUrlIndex < candidatePhotoUrls.size - 1) {
                            currentUrlIndex++
                        }
                    }
                    FallbackMonogram(avatarGradients, initials, borderColor, size)
                }
            )
        } else {
            // Render Islamic Monogram
            FallbackMonogram(avatarGradients, initials, borderColor, size)
        }
    }
}

@Composable
private fun FallbackMonogram(
    gradients: List<Color>,
    initials: String,
    goldColor: Color,
    size: Dp
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(gradients)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.28f).sp,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}
