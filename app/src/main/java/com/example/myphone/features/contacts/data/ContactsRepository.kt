package com.example.myphone.features.contacts.data

import android.content.ContentResolver
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsRepository(private val contentResolver: ContentResolver) {

    suspend fun getContacts(): List<Contact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<Contact>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )

        cursor?.use {
            val idColumn = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameColumn = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val photoUriColumn = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            while (it.moveToNext()) {
                val id = it.getString(idColumn)
                val name = it.getString(nameColumn)
                val photoUri = it.getString(photoUriColumn)
                contacts.add(Contact(id, name, photoUri))
            }
        }
        return@withContext contacts.distinctBy { it.id }
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
                // NORMALIZE the number before adding it to the list.
                val rawNumber = it.getString(numberColumn)
                phoneNumbers.add(normalizePhoneNumber(rawNumber))
            }
        }
        return@withContext ContactDetails(contactId, name, photoUri, phoneNumbers.distinct())
    }

    /**
     * Helper function to clean phone numbers.
     * It removes all non-digit characters except for a leading '+'.
     * Example: "+91 (000) 000-0000" becomes "+910000000000"
     */
    private fun normalizePhoneNumber(number: String): String {
        val isInternational = number.startsWith("+")
        // This regex removes anything that isn't a digit.
        val digitsOnly = number.replace(Regex("\\D"), "")
        return if (isInternational) "+$digitsOnly" else digitsOnly
    }
}
