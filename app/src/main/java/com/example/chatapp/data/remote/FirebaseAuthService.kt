package com.example.chatapp.data.remote

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthService(private val activity: Activity) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val googleSignInClient: GoogleSignInClient
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object {
        private const val WEB_CLIENT_ID = "8717434441-hikha5psn3s6ea629ct5ig4jcs8hm5s6.apps.googleusercontent.com"
        const val RC_SIGN_IN = 9001
    }

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    fun getGoogleSignInIntent(): Intent = googleSignInClient.signInIntent

    suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount): AuthResult? {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        return auth.signInWithCredential(credential).await()
    }

    suspend fun signInWithEmail(email: String, password: String): AuthResult? {
        return auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signUpWithEmail(email: String, password: String, firstName: String, lastName: String, phone: String): AuthResult? {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user
        user?.let {
            val displayName = "$firstName $lastName"
            val userData = hashMapOf(
                "uid" to it.uid,
                "email" to it.email,
                "firstName" to firstName,
                "lastName" to lastName,
                "phone" to phone,
                "displayName" to displayName,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(it.uid).set(userData).await()
        }
        return result
    }

    fun signInWithPhone(credential: PhoneAuthCredential) = auth.signInWithCredential(credential)

    fun getCurrentUser() = auth.currentUser

    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
    }
} 