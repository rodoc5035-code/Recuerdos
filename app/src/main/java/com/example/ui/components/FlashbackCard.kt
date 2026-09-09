package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MemoryEntity
import com.example.ui.theme.BrandPink
import com.example.ui.theme.BrandViolet
import com.example.ui.theme.MemoryGold
import com.example.ui.theme.WhatsAppGreen
import com.example.util.MemoryDate
import com.example.util.ShareUtils
import java.io.File

@Composable
fun FlashbackCard(
    flashbackMemory: MemoryEntity?,
    isAnniversaryToday: Boolean,
    onMemoryClick: (MemoryEntity) -> Unit,
    onRefreshRandom: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (flashbackMemory == null) return

    val context = LocalContext.current
    val memoryDate = MemoryDate.fromIso(flashbackMemory.dateIso)
    val today = MemoryDate.today()
    val yearsDiff = today.year - memoryDate.year

    val headerTitle = if (isAnniversaryToday && yearsDiff > 0) {
        if (yearsDiff == 1) "✨ Un día como hoy hace 1 año" else "✨ Un día como hoy hace $yearsDiff años"
    } else {
        "✨ Cápsula del tiempo • Revive este momento"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("flashback_card")
            .clickable { onMemoryClick(flashbackMemory) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        border = BorderStroke(1.dp, BrandPink.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Title badge and refresh button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAnniversaryToday) Icons.Default.Favorite else Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = BrandPink,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = headerTitle,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp
                    )
                }

                if (!isAnniversaryToday) {
                    IconButton(
                        onClick = onRefreshRandom,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("btn_shuffle_flashback")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Ver otro momento",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body: Thumbnail + Description preview + Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val imageModel = if (flashbackMemory.imageUri.startsWith("/")) {
                    File(flashbackMemory.imageUri)
                } else {
                    flashbackMemory.imageUri
                }

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = flashbackMemory.description,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(14.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = memoryDate.formattedDisplay,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (flashbackMemory.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = flashbackMemory.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions row: WhatsApp Direct Share & "Revivir" fullscreen action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Toca para ver en detalle",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )

                FilledTonalButton(
                    onClick = {
                        ShareUtils.shareToWhatsApp(
                            context = context,
                            imagePath = flashbackMemory.imageUri,
                            description = flashbackMemory.description,
                            dateFormatted = memoryDate.formattedDisplay,
                            tags = flashbackMemory.getTagList()
                        )
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = WhatsAppGreen.copy(alpha = 0.14f),
                        contentColor = WhatsAppGreen
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("btn_flashback_whatsapp")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = WhatsAppGreen
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "WhatsApp",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhatsAppGreen
                    )
                }
            }
        }
    }
}
