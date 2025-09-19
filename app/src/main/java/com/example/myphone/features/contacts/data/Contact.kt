package com.example.myphone.features.contacts.data

import java.sql.Date
import java.sql.Time
import kotlin.time.Duration

// This is a simple data class (a model) that represents a single contact.
// Using a data class gives us helpful functions like equals(), hashCode(), and toString() for free.
data class Contact(
    val id: String,
    val name: String,
    // A contact might not have a photo, so we make this nullable.
    val photoUri: String? = null
)
