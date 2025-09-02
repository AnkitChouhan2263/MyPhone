package com.example.myphone

import java.sql.Date
import java.sql.Time
import kotlin.time.Duration


data class Contact(
    var firstName:String,
    var lastName: String,
    var phoneNumber: Number,
    var imageId : Int,
    var history : MutableList<CallEntry>
)

data class CallEntry(
    val callerNumber : Number,
    val date: Date,
    val time: Time,
    val duration: Duration,
    val callType : CallType,
)

enum class CallType {
    Incoming,
    Outgoing,
    Missed,
    Rejected,
}
