package hardkernel.Updater.logic

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.RecoverySystem
import android.util.Log

import java.io.File
import java.io.IOException

public class Loader (val context: Context?) {

    private val debug: Boolean = false

    @Throws(IOException::class)
    fun updateFromLocal(uri: Uri): Boolean {
        val packageFile = File(getPathFrom(uri))

        try {
            RecoverySystem.installPackage(context, packageFile)
        } catch (e: IOException) {
            throw IOException(e)
        }
        return true
    }

    fun getPathFrom(uri: Uri): String {
        if (debug)
            Log.d("Loader", "This is uri - " + uri.getPath())
        val cursor: Cursor = context!!.getContentResolver().query(uri, null, null, null, null)

        if (debug)
            Log.d("Loader", cursorToString(cursor))
        val path = getRealPathFrom(cursor)
        return path
    }

    private fun getRealPathFrom(cursor: Cursor):String {
        var realPath = ""
        if(cursor.moveToFirst()) {
            val doc_id = cursor.getString(cursor.getColumnIndex("document_id"))
            val subDocId = doc_id.split(":")

            realPath += when (subDocId[0]) {
                "primary" -> {
                    "/data/media/0/" + subDocId[1]
                }
                else -> {
                    // Todo change path fit realPath
                    "/storage/" + subDocId[0] + "/" + subDocId[1]
                }
            }
        }
        return realPath
    }

    fun cursorToString(cursor: Cursor): String {
        var cursorString = "";
        if (cursor.moveToFirst()) {
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
