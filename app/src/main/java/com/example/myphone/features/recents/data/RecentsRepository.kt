package com.example.myphone.features.recents.data

import android.content.ContentResolver
import android.provider.CallLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class RecentsRepository(private val contentResolver: ContentResolver) {

    suspend fun getCallLog(): List<CallLogEntry> = withContext(Dispatchers.IO) {
        val callLog = mutableListOf<CallLogEntry>()

        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION
        )
        // Sort by date in descending order to show the most recent calls first.
        val sortOrder = "${CallLog.Calls.DATE} DESC"

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
                val name = it.getString(nameColumn) ?: "Unknown"
                val number = it.getString(numberColumn)
                val date = it.getLong(dateColumn)
                val duration = it.getLong(durationColumn)

                val type = when (it.getInt(typeColumn)) {
                    CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
                    CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
                    CallLog.Calls.MISSED_TYPE -> CallType.MISSED
                    else -> CallType.UNKNOWN
                }

                callLog.add(
                    CallLogEntry(
                        id = id,
                        name = name,
                        number = number,
                        type = type,
                        date = formatDate(date),
                        duration = duration
                    )
                )
            }
        }
        return@withContext callLog
    }

    private fun formatDate(dateInMillis: Long): String {
        val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        return formatter.format(Date(dateInMillis))
    }
}
