package com.example.chatapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatapp.presentation.auth.AuthScreen
import com.example.chatapp.presentation.home.HomeScreen
import com.example.chatapp.presentation.splash.SplashScreen
import com.example.chatapp.presentation.viewmodel.AuthViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        authViewModel = AuthViewModel(this)
        setContent {
            val navController = rememberNavController()
            ChatAppNavHost(
                navController = navController,
                authViewModel = authViewModel,
                onSendCode = { phoneNumber -> startPhoneNumberVerification(phoneNumber) },
                onVerifyCode = { code ->
                    val credential = PhoneAuthProvider.getCredential(storedVerificationId ?: "", code)
                    authViewModel.signInWithPhone(credential)
                }
            )
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder()
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            authViewModel.signInWithPhone(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(this@MainActivity, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            storedVerificationId = verificationId
            resendToken = token
            Toast.makeText(this@MainActivity, "Code sent!", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun ChatAppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onSendCode: (String) -> Unit,
    onVerifyCode: (String) -> Unit
) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                onNavigate = { isSignedIn ->
                    navController.navigate(if (isSignedIn) "home" else "auth") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("auth") {
            AuthScreen(
                viewModel = authViewModel,
                onSendCode = onSendCode,
                onVerifyCode = onVerifyCode,
                navController = navController
            )
        }
        composable("home") {
            HomeScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
    }
}