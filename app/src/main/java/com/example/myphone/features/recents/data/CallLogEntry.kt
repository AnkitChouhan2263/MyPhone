package com.example.myphone.features.recents.data

/**
 * Represents a single entry in the call log.
 *
 * @param id The unique ID of the call log entry.
 * @param name The contact's name. "Unknown" if the number is not in contacts.
 * @param number The phone number associated with the call.
 * @param type The type of the call (Incoming, Outgoing, Missed, etc.).
 * @param date A formatted string representing the date and time of the call.
 * @param duration The duration of the call in seconds.
 * @param photoUri The URI for the contact's photo, if available.
 */
data class CallLogEntry(
    val id: String,
    val contactId: String?, // The ID of the contact in the address book.
    val name: String,
    val number: String,
    val type: CallType,
    val date: String,
    val duration: Long,
    val photoUri: String? = null
)

/**
 * An enum to represent the different types of calls in a type-safe way.
 */
enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED,
    REJECTED,
    BLOCKED,
    VOICEMAIL, // New type
    ANSWERED_EXTERNALLY, // New type
    UNKNOWN
}

