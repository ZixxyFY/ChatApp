package com.example.chatapp.presentation.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigate: (isSignedIn: Boolean) -> Unit) {
    LaunchedEffect(Unit) {
        delay(1500)
        val isSignedIn = FirebaseAuth.getInstance().currentUser != null
        onNavigate(isSignedIn)
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "ChatBot",
            style = MaterialTheme.typography.displayLarge
        )
    }
} 