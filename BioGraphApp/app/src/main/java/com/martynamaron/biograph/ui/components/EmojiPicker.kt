package com.martynamaron.biograph.ui.components

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmojiPicker(
    emoji: String,
    onEmojiChanged: (String) -> Unit,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = emoji,
        onValueChange = { newValue ->
            // Accept only if it looks like a single emoji or is being cleared
            if (newValue.isEmpty() || isSingleEmoji(newValue)) {
                onEmojiChanged(newValue)
            }
        },
        label = { Text("Emoji") },
        placeholder = { Text("🙂") },
        textStyle = TextStyle(fontSize = 28.sp),
        singleLine = true,
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        modifier = modifier.width(100.dp)
    )
}

private fun isSingleEmoji(text: String): Boolean {
    if (text.isEmpty()) return false
    // Count grapheme clusters — a single emoji (including ZWJ sequences) is 1 cluster
    val codePointCount = text.codePointCount(0, text.length)
    // Allow up to a few code points for compound emoji (flags, skin tones, ZWJ sequences)
    return codePointCount in 1..7 && text.trim().length == text.length
}
