package com.example.myphone.features.contacts.data

// This is our new, more detailed data model for the details screen.
// It's separate from the simple 'Contact' model used for the list.
data class ContactDetails(
    val id: String,
    val name: String,
    val photoUri: String?,
    val phoneNumbers: List<String>
)
