package com.example.myphone.features.contacts.data

/**
 * A detailed data model for a single contact.
 *
 * @param id The unique ID of the contact.
 * @param name The contact's display name.
 * @param photoUri A string URI for the contact's photo, if available.
 * @param phoneNumbers A list of all phone numbers for the contact.
 * @param isFavorite True if the contact is starred/favorited, false otherwise.
 */
data class ContactDetails(
    val id: String,
    val name: String,
    val photoUri: String? = null,
    val phoneNumbers: List<String>,
    val isFavorite: Boolean = false
)
