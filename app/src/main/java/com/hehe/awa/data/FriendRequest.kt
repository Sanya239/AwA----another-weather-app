package com.hehe.awa.data

import java.util.Date

data class FriendRequest(
    val id: String,
    val fromUid: String,
    val toUid: String,
    val status: RequestStatus,
    val createdAt: Date,
)

enum class RequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

