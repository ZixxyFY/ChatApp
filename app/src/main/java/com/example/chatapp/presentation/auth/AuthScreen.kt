package com.example.chatapp.presentation.auth

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chatapp.presentation.viewmodel.AuthState
import com.example.chatapp.presentation.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@Composable
fun AuthScreen(
    viewModel: AuthViewModel? = null,
    onSendCode: (String) -> Unit = {},
    onVerifyCode: (String) -> Unit = {},
    navController: NavController? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val authViewModel = viewModel ?: remember { activity?.let { AuthViewModel(it) } }
    val authState by authViewModel?.authState?.collectAsState() ?: remember { mutableStateOf(AuthState.Idle) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var showPasswordScreen by remember { mutableStateOf(false) }
    var showPhoneScreen by remember { mutableStateOf(false) }
    var showSignUpScreen by remember { mutableStateOf(false) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var signUpPhone by remember { mutableStateOf("") }

    val googleSignInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data: Intent? = result.data
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                authViewModel?.signInWithGoogle(account)
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    // Navigate to Home on successful sign-in
    if (authState is AuthState.Success && navController != null) {
        LaunchedEffect(authState) {
            navController.navigate("home") {
                popUpTo("auth") { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to ChatBot", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(32.dp))

            if (showPasswordScreen) {
                // Password entry screen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showPasswordScreen = false }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                    Text("Back", color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Enter your password", color = MaterialTheme.colorScheme.onBackground)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { authViewModel?.signInWithEmail(email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Continue", color = Color.Black)
                }
                if (authState is AuthState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showSignUpScreen = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Sign Up with Email", color = Color.Black)
                    }
                }
            } else if (showSignUpScreen) {
                // Sign up details screen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showSignUpScreen = false; showPasswordScreen = true }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                    Text("Back", color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = signUpPhone,
                    onValueChange = { signUpPhone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        authViewModel?.signUpWithEmail(email, password, firstName, lastName, signUpPhone)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Create Account", color = Color.Black)
                }
            } else if (showPhoneScreen) {
                // Phone entry screen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showPhoneScreen = false }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                    Text("Back", color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onSendCode(phone) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Send Code", color = Color.Black)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Verification Code") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = { onVerifyCode(code) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Verify Code", color = Color.Black)
                }
            } else {
                // Main auth options
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { if (email.isNotBlank()) showPasswordScreen = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Email, contentDescription = "Email", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue with Email", color = Color.Black)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val intent = authViewModel?.getGoogleSignInIntent()
                        if (intent != null) googleSignInLauncher.launch(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("G", color = Color.Black) // Placeholder for Google icon
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue with Google", color = Color.Black)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showPhoneScreen = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "Phone", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue with Phone", color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (authState) {
                is AuthState.Loading -> CircularProgressIndicator()
                is AuthState.Error -> Text((authState as AuthState.Error).message, color = MaterialTheme.colorScheme.error)
                is AuthState.Success -> Text("Signed in as: " + (authState as AuthState.Success).user?.email.orEmpty())
                else -> {}
            }
        }
    }
} 