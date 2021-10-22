package hardkernel.Updater.Logic

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.IOException

class LocalLoader (private val context: Context, private val uri: Uri) {
    private val debug: Boolean = true

    val file get() = File(getPathFrom())

    @Throws(IOException::class)
    private fun getPathFrom(): String {
        if (debug)
            Log.d("Loader", "This is uri - " + uri.path)
        val cursor = context!!.getContentResolver().query(uri, null, null, null, null)

        if (debug)
            Log.d("Loader", cursorToString(cursor))

        try {
            return getRealPathFrom(cursor)
        } catch (e:Exception) {
            throw IOException(e)
        }
    }

    @Throws(IOException::class)
    private fun getRealPathFrom(cursor: Cursor?):String {
        var realPath = ""
        if(cursor?.moveToFirst() == true) {
            val doc_id = cursor.getString(cursor.getColumnIndex("document_id"))
            val subDocId = doc_id.split(":")

            realPath += when (subDocId[0]) {
                "primary" -> {
                    "/data/media/0/" + subDocId[1]
                }
                "raw" -> {
                    val replacedPath = if (subDocId[1].startsWith("/storage/emulated/0"))
                        "/data/media/0" +  subDocId[1].removePrefix("/storage/emulated/0")
                    else
                        subDocId[1]
                    replacedPath
                }
                "msf" -> {
                    throw IOException("msf: format is not implemented yet")
                }
                else -> {
                    // Todo change path fit realPath
                    "/storage/" + subDocId[0] + "/" + subDocId[1]
                }
            }
        }
        if (debug)
            Log.d("Loader", "real path - " + realPath)
        return realPath
    }

    private fun cursorToString(cursor: Cursor?): String {
        var cursorString = "";
        if (cursor?.moveToFirst() == true) {
            val columnNames = cursor.getColumnNames()
            for(name in columnNames)
                cursorString += String.format("%s ][ ", name)
            cursorString += "\n"
            do {
                for (name in columnNames) {
                    cursorString += String.format("%s ][ ", cursor.getString(cursor.getColumnIndex(name)))
                }
                cursorString += "\n"
            } while (cursor.moveToNext())
        }
        return cursorString;
    }
}