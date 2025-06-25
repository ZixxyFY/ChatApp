package com.example.chatapp.presentation.viewmodel

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.MainActivity
import com.example.chatapp.R
import com.example.chatapp.data.remote.FirebaseAuthService
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser?) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val activity: Activity) : ViewModel() {
    private val authService = FirebaseAuthService(activity)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private fun showLoginNotification() {
        val channelId = "login_notifications"
        val notificationId = 100
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Login Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = activity.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        val intent = Intent(activity, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(activity, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Login Successful")
            .setContentText("You have successfully logged in.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(activity)) {
            notify(notificationId, builder.build())
        }
    }

    fun getGoogleSignInIntent(): Intent = authService.getGoogleSignInIntent()

    fun signInWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result: AuthResult? = authService.firebaseAuthWithGoogle(account)
                _authState.value = AuthState.Success(result?.user)
                showLoginNotification()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Google sign-in failed")
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authService.signInWithEmail(email, password)
                _authState.value = AuthState.Success(result?.user)
                showLoginNotification()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Email sign-in failed")
            }
        }
    }

    fun signUpWithEmail(email: String, password: String, firstName: String, lastName: String, phone: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authService.signUpWithEmail(email, password, firstName, lastName, phone)
                _authState.value = AuthState.Success(result?.user)
                showLoginNotification()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Email sign-up failed")
            }
        }
    }

    fun signInWithPhone(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authService.signInWithPhone(credential).await()
                _authState.value = AuthState.Success(result.user)
                showLoginNotification()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Phone sign-in failed")
            }
        }
    }

    fun signOut() {
        authService.signOut()
        _authState.value = AuthState.Idle
    }
} 