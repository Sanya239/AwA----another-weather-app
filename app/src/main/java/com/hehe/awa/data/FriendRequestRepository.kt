package com.hehe.awa.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

class FriendRequestRepository {
    private val db = FirebaseFirestore.getInstance()
    private val requestsCollection = db.collection("friend_requests")
    private val userTagsCollection = db.collection("user_tags")

    suspend fun findUserByTag(tag: String): String? {
        val tagDoc = userTagsCollection.document(tag).get().await()
        return if (tagDoc.exists()) {
            tagDoc.getString("uid")
        } else {
            null
        }
    }

    suspend fun createRequest(fromUid: String, toUid: String): UpdateResult {
        return try {
            val requestData = mapOf(
                "fromUid" to fromUid,
                "toUid" to toUid,
                "status" to RequestStatus.PENDING.name.lowercase(),
                "createdAt" to Date()
            )
            requestsCollection.add(requestData).await()
            UpdateResult.Success
        } catch (e: Exception) {
            UpdateResult.Error(e.message, e)
        }
    }

    suspend fun getPendingRequests(uid: String): List<FriendRequest> {
        val outgoingQuery = requestsCollection
            .whereEqualTo("fromUid", uid)
            .whereEqualTo("status", RequestStatus.PENDING.name.lowercase())
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()

        val incomingQuery = requestsCollection
            .whereEqualTo("toUid", uid)
            .whereEqualTo("status", RequestStatus.PENDING.name.lowercase())
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()

        val requests = mutableListOf<FriendRequest>()
        
        outgoingQuery.documents.forEach { doc ->
            requests.add(
                FriendRequest(
                    id = doc.id,
                    fromUid = doc.getString("fromUid") ?: "",
                    toUid = doc.getString("toUid") ?: "",
                    status = RequestStatus.PENDING,
                    createdAt = doc.getDate("createdAt") ?: Date()
                )
            )
        }

        incomingQuery.documents.forEach { doc ->
            requests.add(
                FriendRequest(
                    id = doc.id,
                    fromUid = doc.getString("fromUid") ?: "",
                    toUid = doc.getString("toUid") ?: "",
                    status = RequestStatus.PENDING,
                    createdAt = doc.getDate("createdAt") ?: Date()
                )
            )
        }

        return requests
    }

    suspend fun acceptRequest(requestId: String): UpdateResult {
        return try {
            requestsCollection.document(requestId)
                .update("status", RequestStatus.ACCEPTED.name.lowercase())
                .await()
            UpdateResult.Success
        } catch (e: Exception) {
            UpdateResult.Error(e.message, e)
        }
    }

    suspend fun rejectRequest(requestId: String): UpdateResult {
        return try {
            requestsCollection.document(requestId)
                .update("status", RequestStatus.REJECTED.name.lowercase())
                .await()
            UpdateResult.Success
        } catch (e: Exception) {
            UpdateResult.Error(e.message, e)
        }
    }

    suspend fun removeFriend(userUid: String, friendUid: String): UpdateResult {
        return try {
            val outgoingQuery = requestsCollection
                .whereEqualTo("fromUid", userUid)
                .whereEqualTo("toUid", friendUid)
                .whereEqualTo("status", RequestStatus.ACCEPTED.name.lowercase())
                .get()
                .await()

            val incomingQuery = requestsCollection
                .whereEqualTo("fromUid", friendUid)
                .whereEqualTo("toUid", userUid)
                .whereEqualTo("status", RequestStatus.ACCEPTED.name.lowercase())
                .get()
                .await()

            val allRequests = outgoingQuery.documents + incomingQuery.documents

            allRequests.forEach { doc ->
                requestsCollection.document(doc.id)
                    .update("status", RequestStatus.REJECTED.name.lowercase())
                    .await()
            }

            UpdateResult.Success
        } catch (e: Exception) {
            UpdateResult.Error(e.message, e)
        }
    }
}

