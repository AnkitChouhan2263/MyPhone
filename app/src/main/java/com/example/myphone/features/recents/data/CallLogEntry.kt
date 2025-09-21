package com.example.myphone.features.recents.data

/**
 * Represents a single entry in the call log.
 *
 * @param id The unique ID of the call log entry.
 * @param name The contact's name. "Unknown" if the number is not in contacts.
 * @param number The phone number associated with the call.
 * @param type The type of the call (Incoming, Outgoing, Missed).
 * @param date A formatted string representing the date and time of the call.
 * @param duration The duration of the call in seconds.
 */
data class CallLogEntry(
    val id: String,
    val name: String,
    val number: String,
    val type: CallType,
    val date: String,
    val duration: Long
)

/**
 * An enum to represent the different types of calls in a type-safe way.
 */
enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED,
    UNKNOWN
}
