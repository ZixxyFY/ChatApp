package com.example.chatapp.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chatapp.domain.model.ChatMessage
import com.example.chatapp.presentation.viewmodel.AuthViewModel
import com.example.chatapp.presentation.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ChatViewModel = ChatViewModel(),
    navController: NavController? = null,
    authViewModel: AuthViewModel? = null
) {
    val messages by viewModel.messages.collectAsState()
    var input by remember { mutableStateOf("") }
    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (input.isNotBlank()) {
                        viewModel.sendMessage(input)
                        input = ""
                    }
                }) {
                    Text("Send")
                }
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ChatBot") },
                actions = {
                    Button(
                        onClick = {
                            authViewModel?.signOut()
                            navController?.navigate("auth") {
                                popUpTo("home") { inclusive = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Sign Out", color = Color.White)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(8.dp),
                reverseLayout = true
            ) {
                items(messages.reversed()) { message ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            color = if (message.isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.medium,
                            tonalElevation = 2.dp,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = message.message,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.takeIf { message.isUser } ?: MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
} 