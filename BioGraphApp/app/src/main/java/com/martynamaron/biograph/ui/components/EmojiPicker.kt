package com.martynamaron.biograph.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.emoji2.emojipicker.EmojiPickerView

@Composable
fun EmojiPicker(
    emoji: String,
    onEmojiChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    var showEmojiPicker by remember { mutableStateOf(false) }

    val borderColor = if (isError) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.outline
    }

    // Clickable box that shows current emoji and opens picker
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .width(100.dp)
            .height(56.dp)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable { showEmojiPicker = true }
            .padding(12.dp)
    ) {
        Text(
            text = emoji.ifEmpty { "🙂" },
            fontSize = 28.sp,
            color = if (emoji.isEmpty()) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }

    // Emoji picker dialog
    if (showEmojiPicker) {
        Dialog(onDismissRequest = { showEmojiPicker = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                AndroidView(
                    factory = { context ->
                        EmojiPickerView(context).apply {
                            setOnEmojiPickedListener { emojiViewItem ->
                                onEmojiChanged(emojiViewItem.emoji)
                                showEmojiPicker = false
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                        .height(350.dp)
                )
            }
        }
    }
}
