package com.example.myphone.features.recents.data

import android.content.ContentResolver
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class RecentsRepository(private val contentResolver: ContentResolver) {

    /**
     * This function has been rewritten to be highly performant.
     * It now fetches all call log entries and a map of phone numbers to photo URIs
     * in just two queries, then links them in memory to avoid the N+1 query problem.
     */
    suspend fun getCallLog(): List<CallLogEntry> = withContext(Dispatchers.IO) {
        // Step 1: Create a map of all phone numbers to their photo URIs in a single query.
        val phonePhotoMap = getPhoneToPhotoUriMap()

        val callLog = mutableListOf<CallLogEntry>()

        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION
        )
        val sortOrder = "${CallLog.Calls.DATE} DESC"

        // Step 2: Get all call log entries in a single query.
        val cursor = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            val idColumn = it.getColumnIndex(CallLog.Calls._ID)
            val nameColumn = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val numberColumn = it.getColumnIndex(CallLog.Calls.NUMBER)
            val typeColumn = it.getColumnIndex(CallLog.Calls.TYPE)
            val dateColumn = it.getColumnIndex(CallLog.Calls.DATE)
            val durationColumn = it.getColumnIndex(CallLog.Calls.DURATION)

            while (it.moveToNext()) {
                val id = it.getString(idColumn)
                val name = it.getString(nameColumn).takeIf { !it.isNullOrBlank() } ?: "Unknown"
                val number = it.getString(numberColumn) ?: ""
                val date = it.getLong(dateColumn)
                val duration = it.getLong(durationColumn)

                // Step 3: Use the pre-fetched map for a fast, in-memory lookup.
                val photoUri = phonePhotoMap[number]

                val type = when (it.getInt(typeColumn)) {
                    CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
                    CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
                    CallLog.Calls.MISSED_TYPE -> CallType.MISSED
                    CallLog.Calls.REJECTED_TYPE -> CallType.REJECTED
                    CallLog.Calls.BLOCKED_TYPE -> CallType.BLOCKED
                    CallLog.Calls.VOICEMAIL_TYPE -> CallType.VOICEMAIL
                    CallLog.Calls.ANSWERED_EXTERNALLY_TYPE -> CallType.ANSWERED_EXTERNALLY
                    else -> CallType.UNKNOWN
                }

                callLog.add(
                    CallLogEntry(
                        id = id,
                        name = name,
                        number = number,
                        type = type,
                        date = formatDate(date),
                        duration = duration,
                        photoUri = photoUri
                    )
                )
            }
        }
        return@withContext callLog
    }

    /**
     * A new helper function that efficiently fetches all phone numbers and their
     * corresponding photo URIs, returning them as a map for quick lookups.
     */
    private fun getPhoneToPhotoUriMap(): Map<String, String?> {
        val map = mutableMapOf<String, String?>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            null
        )
        cursor?.use {
            val numberColumn = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoUriColumn = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
            if (numberColumn != -1 && photoUriColumn != -1) {
                while (it.moveToNext()) {
                    val number = it.getString(numberColumn)
                    val photoUri = it.getString(photoUriColumn)
                    if (number != null) {
                        map[number] = photoUri
                    }
                }
            }
        }
        return map
    }

    private fun formatDate(dateInMillis: Long): String {
        val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        return formatter.format(Date(dateInMillis))
    }
}

