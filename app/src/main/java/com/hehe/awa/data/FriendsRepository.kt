package com.hehe.awa.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class Friend(
    val uid: String,
    val name: String,
    val tag: String?
)

class FriendsRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val requestsCollection = db.collection("friend_requests")

    suspend fun getFriends(uid: String): List<Friend> {
        val outgoingQuery = requestsCollection
            .whereEqualTo("fromUid", uid)
            .whereEqualTo("status", RequestStatus.ACCEPTED.name.lowercase())
            .get()
            .await()

        val incomingQuery = requestsCollection
            .whereEqualTo("toUid", uid)
            .whereEqualTo("status", RequestStatus.ACCEPTED.name.lowercase())
            .get()
            .await()

        val friendUids = mutableSetOf<String>()

        outgoingQuery.documents.forEach { doc ->
            val toUid = doc.getString("toUid")
            if (toUid != null) {
                friendUids.add(toUid)
            }
        }

        incomingQuery.documents.forEach { doc ->
            val fromUid = doc.getString("fromUid")
            if (fromUid != null) {
                friendUids.add(fromUid)
            }
        }

        val friends = mutableListOf<Friend>()

        friendUids.forEach { friendUid ->
            val friendDoc = usersCollection.document(friendUid).get().await()
            if (friendDoc.exists()) {
                val name = friendDoc.getString("name") ?: ""
                val tag = friendDoc.getString("tag")
                friends.add(Friend(uid = friendUid, name = name, tag = tag))
            }
        }

        return friends
    }
}

