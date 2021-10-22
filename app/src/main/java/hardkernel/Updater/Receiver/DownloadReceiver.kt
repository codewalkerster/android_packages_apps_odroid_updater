package hardkernel.Updater.Receiver

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.net.Uri
import android.util.Log
import hardkernel.Updater.Service.UpdateService
import java.io.File

class DownloadReceiver : BroadcastReceiver() {

    companion object {
        var enqueue:Long = 0
    }
    private val TAG: String = this.javaClass.name

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L)
        if (id != enqueue) {
            Log.v(TAG, "Ignore unrelated download $id")
            return
        }

        val query = DownloadManager.Query()
        query.setFilterById(id)
        val manager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val cursor = manager.query(query)

        if (!cursor.moveToFirst()) {
            Log.e(TAG, "Not able to move the cursor for downloaded content.")
            return
        }

        val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))

        if (DownloadManager.ERROR_INSUFFICIENT_SPACE == status) {
            Log.e(TAG, "Download is failed due to insufficient space.")
            return
        }

        if (DownloadManager.STATUS_SUCCESSFUL != status) {
            Log.e(TAG, "Download Failed")
            return
        }
        val uri = Uri.parse(cursor.getString(
            cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)))
        val file = File(uri.path)
        if (!file.exists()) {
            Log.e(TAG, "Not able to find downloaded file: " + uri.path)
            return
        }

        val installIntent = Intent(context, UpdateService::class.java)
        installIntent.action = UpdateService.CMD.REMOTE_INSTALL.name
        installIntent.putExtra(UpdateService.CMD.REMOTE_INSTALL.name, file)
        context.startService(installIntent)
    }
}