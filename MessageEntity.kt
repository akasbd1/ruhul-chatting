package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val messageId: Long = 0,
    val chatId: String,
    val senderId: String,
    val text: String,
    val timestamp: String,
    val isSentByMe: Boolean,
    val messageType: String = "text" // text, image, audio, video
)
