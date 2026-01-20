package com.hehe.awa.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

sealed class UpdateResult {
    object Success : UpdateResult()

    data class Error(
        val message: String?,
        val cause: Throwable? = null
    ) : UpdateResult()
}


class UserProfileRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun getOrCreate(uid: String, fallbackName: String?): UserProfile {
        val docRef = usersCollection.document(uid)
        val snap = docRef.get().await()
        if (snap.exists()) {
            val name = snap.getString("name") ?: ""
            val isPrivate = snap.getBoolean("isPrivate") ?: false
            val tag = snap.getString("tag")
            return UserProfile(name = name, isPrivate = isPrivate, tag = tag)
        }

        val profile = UserProfile(name = fallbackName.orEmpty(), isPrivate = false, tag = null)
        docRef.set(
            mapOf(
                "name" to profile.name,
                "isPrivate" to profile.isPrivate,
            ),
            SetOptions.merge(),
        ).await()
        return profile
    }

    suspend fun save(uid: String, profile: UserProfile): UpdateResult {
        val data = mutableMapOf<String, Any>(
            "name" to profile.name,
            "isPrivate" to profile.isPrivate,
        )
        if (profile.tag != null) {
            data["tag"] = profile.tag
        }
        try {
            usersCollection.document(uid).set(data, SetOptions.merge()).await()
        }
        catch (t: Throwable){
            return UpdateResult.Error(t.message, t);
        }
        return UpdateResult.Success
    }

    suspend fun updateTag(uid: String, newTag: String): UpdateResult {
        val userRef = db.collection("users").document(uid)
        var result: UpdateResult = UpdateResult.Success
        db.runTransaction { tx ->
            val userSnap = tx.get(userRef)
            val oldTag = userSnap.getString("tag")

            if (newTag.isEmpty()) {
                if (oldTag != null) {
                    tx.delete(db.collection("user_tags").document(oldTag))
                    tx.update(userRef, "tag", null)
                }
            } else {
                val newTagRef = db.collection("user_tags").document(newTag)
                val newTagSnap = tx.get(newTagRef)
                if (newTagSnap.exists()) {
                    result = UpdateResult.Error("Tag already taken")
                    return@runTransaction
                }

                try {
                    tx.set(newTagRef, mapOf("uid" to uid))
                    tx.update(userRef, "tag", newTag)
                    if (oldTag != null && oldTag != newTag) {
                        tx.delete(db.collection("user_tags").document(oldTag))
                    }
                }
                catch (t: Throwable){
                    result = UpdateResult.Error(t.message, t)
                }
            }
        }.await()
        return result
    }
}


