package dev.l2j.autobots.models

internal data class BotChat(val chatType: ChatType, val senderName: String, val message: String, val createdDate: Long = System.currentTimeMillis())