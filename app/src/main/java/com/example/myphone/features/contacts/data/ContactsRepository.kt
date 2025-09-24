package com.example.myphone.features.contacts.data

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.ContentValues
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsRepository(private val contentResolver: ContentResolver) {

    /**
     * This function has been rewritten to be "contact-centric".
     * It now fetches all contacts first, then fetches numbers for each contact if they exist.
     * This ensures contacts without phone numbers are included in the list.
     */
    /**
     * This function has been rewritten to be highly performant.
     * It now fetches all contacts and all phone numbers in just two queries,
     * then links them together in memory to avoid the N+1 query problem.
     */
    suspend fun getContacts(): List<Contact> = withContext(Dispatchers.IO) {
        // Use a map to hold the contacts, keyed by their ID for quick lookups.
        val contactsMap = mutableMapOf<String, Contact>()

        // --- Query 1: Fetch ALL contacts ---
        // Add STARRED to the projection to get favorite status.
        val contactProjection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.PHOTO_URI,
            ContactsContract.Contacts.STARRED
        )
        val contactCursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            contactProjection,
            null,
            null,
            "${ContactsContract.Contacts.DISPLAY_NAME} ASC"
        )

        contactCursor?.use {
            val idColumn = it.getColumnIndex(ContactsContract.Contacts._ID)
            val nameColumn = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val photoUriColumn = it.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)
            val starredColumn = it.getColumnIndex(ContactsContract.Contacts.STARRED)


            while (it.moveToNext()) {
                val id = it.getString(idColumn)
                val name = it.getString(nameColumn) ?: "No Name"
                val photoUri = it.getString(photoUriColumn)
                val isFavorite = it.getInt(starredColumn) == 1

                // Initially, create the contact with an empty list of numbers.
                contactsMap[id] = Contact(id, name, photoUri, emptyList(), isFavorite)
            }
        }

        // --- Query 2: Fetch ALL phone numbers in a single go ---
        val phoneProjection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val phoneCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            phoneProjection,
            null,
            null,
            null
        )

        phoneCursor?.use {
            val contactIdColumn = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val numberColumn = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val contactId = it.getString(contactIdColumn)
                val number = it.getString(numberColumn)
                // Find the contact in our map that this number belongs to.
                val contact = contactsMap[contactId]
                if (contact != null) {
                    // Add the number to the contact's list of numbers.
                    val updatedNumbers = contact.numbers + number
                    contactsMap[contactId] = contact.copy(numbers = updatedNumbers)
                }
            }
        }
        return@withContext contactsMap.values.toList()

    }

    suspend fun getContactDetails(contactId: String): ContactDetails = withContext(Dispatchers.IO) {
        var name = ""
        var photoUri: String? = null
        var isFavorite = false
        val phoneNumbers = mutableListOf<String>()

        val contactCursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_URI,
                ContactsContract.Contacts.STARRED
            ),
            "${ContactsContract.Contacts._ID} = ?",
            arrayOf(contactId),
            null
        )

        contactCursor?.use {
            if (it.moveToFirst()) {
                name = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                photoUri = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI))
                isFavorite = it.getInt(it.getColumnIndexOrThrow(ContactsContract.Contacts.STARRED)) == 1
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
        return@withContext ContactDetails(contactId, name, photoUri, phoneNumbers.distinct(), isFavorite)
    }

    /**
     * Sets the favorite (starred) status for a given contact.
     */
    suspend fun setFavoriteStatus(contactId: String, isFavorite: Boolean): Boolean = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(ContactsContract.Contacts.STARRED, if (isFavorite) 1 else 0)
        }
        val selection = "${ContactsContract.Contacts._ID} = ?"
        val selectionArgs = arrayOf(contactId)

        return@withContext try {
            val rowsUpdated = contentResolver.update(
                ContactsContract.Contacts.CONTENT_URI,
                values,
                selection,
                selectionArgs
            )
            rowsUpdated > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun addContact(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        phoneType: Int,
        email: String,
        emailType: Int
    ): Boolean = withContext(Dispatchers.IO) {
        // A list to hold all the operations for this transaction.
        val ops = ArrayList<ContentProviderOperation>()

        // Create a new raw contact.
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )

        // Add the contact's name.
        if (firstName.isNotBlank() || lastName.isNotBlank()) {
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastName)
                    .build()
            )
        }

        // Add the phone number.
        if (phoneNumber.isNotBlank()) {
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, phoneType)
                    .build()
            )
        }

        // Add the email address.
        if (email.isNotBlank()) {
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, email)
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, emailType)
                    .build()
            )
        }

        // Execute all the operations as a single transaction.
        return@withContext try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            true
        } catch (e: Exception) {
            // If anything fails, the transaction is rolled back.
            e.printStackTrace()
            false
        }
    }

    /**
     * This function has been rewritten to correctly handle both updating an existing number
     * and inserting a number if one doesn't already exist ("upsert" logic).
     */
    /**
     * This function has been completely rewritten with robust "upsert/delete" logic
     * to definitively fix the bug where saving an edit would fail.
     */
    suspend fun updateContact(
        contactId: String,
        firstName: String,
        lastName: String,
        phoneNumber: String
    ): Boolean = withContext(Dispatchers.IO) {
        val ops = ArrayList<ContentProviderOperation>()
        val nameSelection = "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?"
        val nameSelectionArgs = arrayOf(contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)

        // 1. Update the name (this row always exists).
        ops.add(
            ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(nameSelection, nameSelectionArgs)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastName)
                .build()
        )

        // 2. Check for an existing phone number row for this contact.
        val phoneSelection = "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?"
        val phoneSelectionArgs = arrayOf(contactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
        val phoneCursor = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.Data._ID), // We only need to know if it exists
            phoneSelection,
            phoneSelectionArgs,
            null
        )

        if (phoneCursor != null && phoneCursor.moveToFirst()) {
            // A phone row exists.
            val phoneRowId = phoneCursor.getString(0)
            if (phoneNumber.isNotBlank()) {
                // 3a. If it exists AND the new number is not blank, UPDATE it.
                ops.add(
                    ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection("${ContactsContract.Data._ID} = ?", arrayOf(phoneRowId))
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                        .build()
                )
            } else {
                // 3b. If it exists AND the new number is blank, DELETE it.
                ops.add(
                    ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                        .withSelection("${ContactsContract.Data._ID} = ?", arrayOf(phoneRowId))
                        .build()
                )
            }
        } else if (phoneNumber.isNotBlank()) {
            // 4. A phone row DOES NOT exist and the new number is not blank, so INSERT it.
            // We must find a raw contact ID to associate the new number with.
            val rawContactId = getRawContactId(contactId)
            if (rawContactId != null) {
                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                        .build()
                )
            }
        }
        phoneCursor?.close()

        return@withContext try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Deletes a contact from the device based on its contact ID.
     * This is a destructive and irreversible operation.
     */
    suspend fun deleteContact(contactId: String): Boolean = withContext(Dispatchers.IO) {
        val selection = "${ContactsContract.RawContacts.CONTACT_ID} = ?"
        val selectionArgs = arrayOf(contactId)

        return@withContext try {
            // Deleting the raw contact also deletes all associated data (name, phone, etc.)
            val rowsDeleted = contentResolver.delete(
                ContactsContract.RawContacts.CONTENT_URI,
                selection,
                selectionArgs
            )
            // Return true if at least one row was successfully deleted.
            rowsDeleted > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Helper function to find a RawContact ID for a given Contact ID.
     * This is necessary to correctly link new data rows to an existing contact.
     */
    private fun getRawContactId(contactId: String): String? {
        val selection = "${ContactsContract.RawContacts.CONTACT_ID} = ?"
        val selectionArgs = arrayOf(contactId)
        val cursor = contentResolver.query(
            ContactsContract.RawContacts.CONTENT_URI,
            arrayOf(ContactsContract.RawContacts._ID),
            selection,
            selectionArgs,
            null
        )
        var rawContactId: String? = null
        if (cursor != null && cursor.moveToFirst()) {
            rawContactId = cursor.getString(0)
        }
        cursor?.close()
        return rawContactId
    }

    private fun normalizePhoneNumber(number: String): String {
        val isInternational = number.startsWith("+")
        val digitsOnly = number.replace(Regex("\\D"), "")
        return if (isInternational) "+$digitsOnly" else digitsOnly
    }
}

