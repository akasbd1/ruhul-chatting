package com.example.data

import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatDao: ChatDao) {
    val allChats: Flow<List<ChatEntity>> = chatDao.getAllChats()

    fun getMessages(chatId: String): Flow<List<MessageEntity>> {
        return chatDao.getMessagesForChat(chatId)
    }

    suspend fun sendMessage(message: MessageEntity) {
        chatDao.insertMessage(message)
        // Update chat list last message summary
        // For simplicity, we can insert/update chat entity as well
    }

    suspend fun saveChat(chat: ChatEntity) {
        chatDao.insertChat(chat)
    }

    suspend fun getUserProfile(): UserEntity? {
        return chatDao.getUserProfile()
    }

    suspend fun saveUserProfile(user: UserEntity) {
        chatDao.saveUserProfile(user)
    }

    suspend fun seedInitialData() {
        // Seed default chats if empty
        val profile = chatDao.getUserProfile()
        if (profile == null) {
            chatDao.saveUserProfile(
                UserEntity(
                    name = "Ruhul Amin",
                    phoneOrEmail = "+880 1700 000000",
                    statusMessage = "Available on RUHUL CHATTING 💬",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200"
                )
            )
        }
    }
}
