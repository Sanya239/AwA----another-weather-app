package com.hehe.awa

import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import java.security.MessageDigest
import java.util.UUID


suspend fun signInWithGoogle(context: android.content.Context): FirebaseUser? {
    val credentialManager = CredentialManager.create(context)
    val auth = Firebase.auth

    val rawNonce = UUID.randomUUID().toString()
    val bytes = rawNonce.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

    val webClientId = context.getString(R.string.web_client_id)

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .setNonce(hashedNonce)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val result = credentialManager.getCredential(
            request = request,
            context = context,
        )

        val credential = result.credential
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val googleIdToken = googleIdTokenCredential.idToken

        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        val authResult = auth.signInWithCredential(firebaseCredential)
            .addOnSuccessListener {
                Log.d("SignIn", "signInWithCredential:success")
            }
            .addOnFailureListener { e ->
                Log.w("SignIn", "signInWithCredential:failure", e)
            }

        while (!authResult.isComplete) {
            kotlinx.coroutines.delay(100)
        }

        return if (authResult.isSuccessful) {
            auth.currentUser
        } else {
            null
        }
    } catch (e: GetCredentialException) {
        Log.e("SignIn", "GetCredentialException", e)
        throw e
    } catch (e: Exception) {
        Log.e("SignIn", "Unknown exception", e)
        throw e
    }
}