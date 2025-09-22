package com.example.myphone.features.contacts.data

import android.content.ContentResolver
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsRepository(private val contentResolver: ContentResolver) {

    suspend fun getContacts(): List<Contact> = withContext(Dispatchers.IO) {
        // Use a map to group numbers by contact ID to handle multiple numbers per contact.
        val contactsMap = mutableMapOf<String, Contact>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            null // No initial sort, we will sort the final list in memory.
        )

        cursor?.use {
            val idColumn = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameColumn = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val photoUriColumn = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
            val numberColumn = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val id = it.getString(idColumn)
                val name = it.getString(nameColumn)
                val photoUri = it.getString(photoUriColumn)
                val number = it.getString(numberColumn)

                val existingContact = contactsMap[id]

                if (existingContact == null) {
                    // First time we've seen this contact ID, create a new entry.
                    contactsMap[id] = Contact(
                        id = id,
                        name = name,
                        photoUri = photoUri,
                        numbers = listOfNotNull(number)
                    )
                } else {
                    // We've seen this contact before, add the new number to its list.
                    if (number != null) {
                        contactsMap[id] = existingContact.copy(
                            numbers = existingContact.numbers + number
                        )
                    }
                }
            }
        }
        // Convert the map's values to a list and sort it alphabetically by name.
        return@withContext contactsMap.values.sortedBy { it.name }
    }

    suspend fun getContactDetails(contactId: String): ContactDetails = withContext(Dispatchers.IO) {
        var name = ""
        var photoUri: String? = null
        val phoneNumbers = mutableListOf<String>()

        val contactCursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_URI
            ),
            "${ContactsContract.Contacts._ID} = ?",
            arrayOf(contactId),
            null
        )

        contactCursor?.use {
            if (it.moveToFirst()) {
                name = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                photoUri = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI))
            }
        }

        val phoneCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )

        phoneCursor?.use {
            val numberColumn = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val rawNumber = it.getString(numberColumn)
                phoneNumbers.add(normalizePhoneNumber(rawNumber))
            }
        }
        return@withContext ContactDetails(contactId, name, photoUri, phoneNumbers.distinct())
    }

    private fun normalizePhoneNumber(number: String): String {
        val isInternational = number.startsWith("+")
        val digitsOnly = number.replace(Regex("\\D"), "")
        return if (isInternational) "+$digitsOnly" else digitsOnly
    }
}

