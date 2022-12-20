package com.glass.oceanbs.models

data class Chat (
    val id: Int,
    val workerName: String?,
    val message: String,
    val date: String,
    val hour: String,
    val type: MessageType
)

enum class MessageType {
    CUSTOMER,
    WORKER,
    AUTOMATED
}