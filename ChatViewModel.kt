package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ChatEntity
import com.example.data.ChatRepository
import com.example.data.MessageEntity
import com.example.data.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class Screen {
    object Splash : Screen()
    object Auth : Screen()
    object ChatsList : Screen()
    data class ChatDetail(val chatId: String, val peerName: String, val peerAvatar: String, val isOnline: Boolean) : Screen()
    data class Call(val peerName: String, val peerAvatar: String, val isVideo: Boolean) : Screen()
    object ProfileSettings : Screen()
    object NewChat : Screen()
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ChatRepository

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _chats = MutableStateFlow<List<ChatEntity>>(emptyList())
    val chats: StateFlow<List<ChatEntity>> = _chats.asStateFlow()

    private val _currentMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val currentMessages: StateFlow<List<MessageEntity>> = _currentMessages.asStateFlow()

    private val _userProfile = MutableStateFlow<UserEntity?>(null)
    val userProfile: StateFlow<UserEntity?> = _userProfile.asStateFlow()

    init {
        val chatDao = AppDatabase.getDatabase(application).chatDao()
        repository = ChatRepository(chatDao)

        viewModelScope.launch {
            repository.seedInitialData()
            loadProfile()
            loadChats()
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun login(phoneOrEmail: String, name: String) {
        viewModelScope.launch {
            val user = UserEntity(
                name = name.ifEmpty { "Ruhul User" },
                phoneOrEmail = phoneOrEmail,
                statusMessage = "Using RUHUL CHATTING",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200"
            )
            repository.saveUserProfile(user)
            _userProfile.value = user
            _isAuthenticated.value = true
            
            // Seed initial mock chat if chats are empty
            seedInitialChatIfNeeded()
            
            _currentScreen.value = Screen.ChatsList
        }
    }

    fun logout() {
        _isAuthenticated.value = false
        _currentScreen.value = Screen.Auth
    }

    private suspend fun seedInitialChatIfNeeded() {
        // Add sample chats if none exist
        val sampleChat = ChatEntity(
            chatId = "chat_1",
            peerName = "Tanvir Ahmed",
            peerAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
            lastMessage = "Assalamu Alaikum! How is the project going?",
            timestamp = "10:42 AM",
            unreadCount = 2,
            isOnline = true
        )
        val sampleChat2 = ChatEntity(
            chatId = "chat_2",
            peerName = "Nusrat Jahan",
            peerAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200",
            lastMessage = "Check out this voice note 🎤",
            timestamp = "Yesterday",
            unreadCount = 0,
            isOnline = false
        )
        val sampleChat3 = ChatEntity(
            chatId = "chat_3",
            peerName = "Rahim Chowdhury",
            peerAvatar = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=200",
            lastMessage = "Let's connect on video call later tonight.",
            timestamp = "Tuesday",
            unreadCount = 0,
            isOnline = true
        )
        repository.saveChat(sampleChat)
        repository.saveChat(sampleChat2)
        repository.saveChat(sampleChat3)

        // Seed messages for chat_1
        repository.sendMessage(MessageEntity(chatId = "chat_1", senderId = "peer", text = "Assalamu Alaikum! How is the project going?", timestamp = "10:40 AM", isSentByMe = false))
        repository.sendMessage(MessageEntity(chatId = "chat_1", senderId = "me", text = "Wa Alaikum Assalam! Building RUHUL CHATTING right now with Flutter/Compose style.", timestamp = "10:41 AM", isSentByMe = true))
        repository.sendMessage(MessageEntity(chatId = "chat_1", senderId = "peer", text = "Awesome! Let me know when it's ready.", timestamp = "10:42 AM", isSentByMe = false))
    }

    private fun loadChats() {
        viewModelScope.launch {
            repository.allChats.collectLatest { chatList ->
                _chats.value = chatList
            }
        }
    }

    fun openChat(chatId: String) {
        viewModelScope.launch {
            repository.getMessages(chatId).collectLatest { msgs ->
                _currentMessages.value = msgs
            }
        }
    }

    fun sendMessage(chatId: String, text: String, messageType: String = "text") {
        if (text.isBlank()) return
        viewModelScope.launch {
            val msg = MessageEntity(
                chatId = chatId,
                senderId = "me",
                text = text,
                timestamp = "Just now",
                isSentByMe = true,
                messageType = messageType
            )
            repository.sendMessage(msg)

            // Update chat last message
            val chat = _chats.value.find { it.chatId == chatId }
            if (chat != null) {
                repository.saveChat(chat.copy(lastMessage = text, timestamp = "Just now"))
            }

            // Refresh current messages
            openChat(chatId)
        }
    }

    fun startNewChat(name: String) {
        viewModelScope.launch {
            val newId = "chat_${System.currentTimeMillis()}"
            val newChat = ChatEntity(
                chatId = newId,
                peerName = name,
                peerAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                lastMessage = "Say hello on RUHUL CHATTING!",
                timestamp = "Just now",
                unreadCount = 0,
                isOnline = true
            )
            repository.saveChat(newChat)
            navigateTo(Screen.ChatDetail(newId, name, newChat.peerAvatar, true))
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _userProfile.value = repository.getUserProfile()
        }
    }

    fun updateProfile(name: String, status: String) {
        viewModelScope.launch {
            val current = _userProfile.value
            val updated = current?.copy(name = name, statusMessage = status) ?: UserEntity(
                name = name,
                phoneOrEmail = "+880 1800 000000",
                statusMessage = status,
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200"
            )
            repository.saveUserProfile(updated)
            _userProfile.value = updated
        }
    }
}
