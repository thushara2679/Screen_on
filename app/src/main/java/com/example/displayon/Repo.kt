package com.example.displayon

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.tasks.await

object Repo {
    private val db get() = Firebase.firestore

    suspend fun ensureSignedIn() {
        if (Firebase.auth.currentUser == null) Firebase.auth.signInAnonymously().await()
    }

    /** Target: publish FCM token under the pairing code. */
    suspend fun registerTarget(code: String) {
        ensureSignedIn()
        val token = Firebase.messaging.token.await()
        updateToken(code, token)
    }

    suspend fun updateToken(code: String, token: String) {
        ensureSignedIn()
        db.collection("devices").document(code)
            .set(mapOf("token" to token, "updated" to FieldValue.serverTimestamp()))
            .await()
    }

    /** Controller: ask the target identified by [code] to turn its screen on. */
    suspend fun sendScreenOn(code: String) {
        ensureSignedIn()
        db.collection("commands").add(
            mapOf(
                "deviceCode" to code,
                "action" to "SCREEN_ON",
                "ts" to FieldValue.serverTimestamp()
            )
        ).await()
    }
}
