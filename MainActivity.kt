package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CallScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatsListScreen
import com.example.ui.screens.NewChatScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ChatViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {
    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val isAuthenticated by viewModel.isAuthenticated.collectAsState()
                val chats by viewModel.chats.collectAsState()
                val currentMessages by viewModel.currentMessages.collectAsState()
                val userProfile by viewModel.userProfile.collectAsState()

                when (val screen = currentScreen) {
                    is Screen.Splash -> {
                        SplashScreen(
                            onTimeout = {
                                if (isAuthenticated) {
                                    viewModel.navigateTo(Screen.ChatsList)
                                } else {
                                    viewModel.navigateTo(Screen.Auth)
                                }
                            }
                        )
                    }
                    is Screen.Auth -> {
                        AuthScreen(
                            onLoginSuccess = { phoneOrEmail, name ->
                                viewModel.login(phoneOrEmail, name)
                            }
                        )
                    }
                    is Screen.ChatsList -> {
                        ChatsListScreen(
                            chats = chats,
                            onChatClick = { chatId, peerName, peerAvatar, isOnline ->
                                viewModel.openChat(chatId)
                                viewModel.navigateTo(Screen.ChatDetail(chatId, peerName, peerAvatar, isOnline))
                            },
                            onNewChatClick = {
                                viewModel.navigateTo(Screen.NewChat)
                            },
                            onProfileClick = {
                                viewModel.navigateTo(Screen.ProfileSettings)
                            }
                        )
                    }
                    is Screen.ChatDetail -> {
                        ChatDetailScreen(
                            chatId = screen.chatId,
                            peerName = screen.peerName,
                            peerAvatar = screen.peerAvatar,
                            isOnline = screen.isOnline,
                            messages = currentMessages,
                            onBack = {
                                viewModel.navigateTo(Screen.ChatsList)
                            },
                            onSendMessage = { text ->
                                viewModel.sendMessage(screen.chatId, text)
                            },
                            onAudioCall = {
                                viewModel.navigateTo(Screen.Call(screen.peerName, screen.peerAvatar, false))
                            },
                            onVideoCall = {
                                viewModel.navigateTo(Screen.Call(screen.peerName, screen.peerAvatar, true))
                            }
                        )
                    }
                    is Screen.Call -> {
                        CallScreen(
                            peerName = screen.peerName,
                            peerAvatar = screen.peerAvatar,
                            isVideo = screen.isVideo,
                            onEndCall = {
                                viewModel.navigateTo(Screen.ChatsList)
                            }
                        )
                    }
                    is Screen.ProfileSettings -> {
                        ProfileSettingsScreen(
                            user = userProfile,
                            onBack = {
                                viewModel.navigateTo(Screen.ChatsList)
                            },
                            onSaveProfile = { name, status ->
                                viewModel.updateProfile(name, status)
                            },
                            onLogout = {
                                viewModel.logout()
                            }
                        )
                    }
                    is Screen.NewChat -> {
                        NewChatScreen(
                            onBack = {
                                viewModel.navigateTo(Screen.ChatsList)
                            },
                            onStartChat = { friendName ->
                                viewModel.startNewChat(friendName)
                            }
                        )
                    }
                }
            }
        }
    }
}
