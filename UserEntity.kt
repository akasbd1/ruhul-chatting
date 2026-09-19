package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val userId: String = "current_user",
    val name: String,
    val phoneOrEmail: String,
    val statusMessage: String,
    val avatarUrl: String
)
