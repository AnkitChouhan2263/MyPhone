package com.example.myphone.features.contacts.data

// This is a simple data class (a model) that represents a single contact.
// Using a data class gives us helpful functions like equals(), hashCode(), and toString() for free.
data class Contact(
    val id: String,
    val name: String,
    // A contact might not have a photo, so we make this nullable.
    val photoUri: String? = null
)
