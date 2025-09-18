package com.example.myphone.data

import java.sql.Date
import java.sql.Time
import kotlin.time.Duration


data class Contact(
    var firstName:String,
    var phoneticFirstName:String,
    var lastName: String,
    var phoneticLastName: String,
    var company: String,
    var phoneNumber: Number,
    var imageId : Int,
    var profileDP:Int,
    var profileCallingCard: Int,
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
