package com.example.myphone.features.contacts.data

import android.content.ContentResolver
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// The repository's job is to provide data from a data source.
// Here, the data source is the phone's contacts provider.
// By creating a class for this, we make our code more testable and organized.
class ContactsRepository(private val contentResolver: ContentResolver) {

    // This is the main function that fetches the contacts.
    // It's a 'suspend' function because querying contacts can take time,
    // so it should be done off the main UI thread.
    suspend fun getContacts(): List<Contact> = withContext(Dispatchers.IO) {
        // We run this code on the IO dispatcher, which is optimized for I/O operations like reading from a database.
        val contacts = mutableListOf<Contact>()

        // These are the specific columns of data we want to retrieve for each contact.
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )

        // The 'contentResolver.query' is the standard Android way to get data from a ContentProvider.
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            // We sort the contacts by name in alphabetical order.
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )

        cursor?.use { // 'use' ensures the cursor is closed automatically after we're done with it.
            val idColumn = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)
            val nameColumn = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val photoUriColumn = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            while (it.moveToNext()) {
                val id = it.getString(idColumn)
                val name = it.getString(nameColumn)
                val photoUri = it.getString(photoUriColumn)
                contacts.add(Contact(id, name, photoUri))
            }
        }
        // To keep this example simple, we'll just get the unique contacts by name.
        // In a real app, you'd handle multiple numbers for the same contact more gracefully.
        return@withContext contacts.distinctBy { it.name }
    }
}
