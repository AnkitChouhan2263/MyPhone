package com.example.myphone.features.contacts.data

/**
 * Represents a single contact entry for the main list.
 *
 * @param id The unique ID of the contact.
 * @param name The contact's display name.
 * @param photoUri A string URI for the contact's photo, if available.
 * @param numbers A list of all phone numbers associated with the contact.
 */
data class Contact(
    val id: String,
    val name: String,
    val photoUri: String? = null,
    val numbers: List<String> = emptyList()
)

