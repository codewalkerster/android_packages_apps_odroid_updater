package hardkernel.Updater.Logic

import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.Log
import android.widget.Toast
import hardkernel.Updater.NotifyManager
import hardkernel.Updater.Receiver.DownloadReceiver
import hardkernel.Updater.Service.UpdateService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.Deferred
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.net.URL

class RemoteUpdater(private val context: Context?, url: String) {
    private val url: String
    private lateinit var packageName: String

    private val DOWNLOAD_DIR = "/storage/emulated/0/Download/"
    private val PACKAGE_MAX_SIZE = 1024 * 1024 * 1024 * 3
    private val versionMeta = "latestupdate_11"
    init {
        this.url = if (url.indexOf("https://") != 0) {
            var str = StringBuffer(url)
            str.insert(0, "https://").toString()
        } else
            url
    }

    suspend fun requestPackage(){
        if (checkVersion()) {
            if (sufficientSpace()) {
                val intent = Intent(context, UpdateService::class.java)
                intent.action = UpdateService.CMD.REMOTE_UPDATE_DOWNLOAD.name

                val message = "Do you want to download new update package?\n" +
                        "It would take a few minutes or hours depends on your network speed.\n"
                if (context != null) {
                    NotifyManager.createChannel(context)
                    NotifyManager.buildNNotify(
                        context,
                        "New update package is found!", message,
                        "Download", intent
                    )
                }
            }
        }
    }

    private fun sufficientSpace(): Boolean {
        val stat = StatFs(DOWNLOAD_DIR)
        val available = stat.availableBytes

        return if (available < PACKAGE_MAX_SIZE) {
            Toast.makeText(context,
            "Check free space\n" +
            "Insufficient free space\n" +
            PACKAGE_MAX_SIZE /1024 /1024 /1024 +
            "GBytes free space is required.", Toast.LENGTH_LONG).show()
            false
        } else
            true
    }

    fun requestDownload() = CoroutineScope(Dispatchers.IO).launch {
        val uri = Uri.parse(url + packageName)

        val request = DownloadManager.Request(uri)
        request.setTitle("Downloading new update package")
        request.setDescription(uri.path)
        request.setNotificationVisibility(
            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationUri(localUri())

        Log.d(this.javaClass.name,
            "Requesting to download " + uri.path + " to " + localUri())

        val oldPackage = File(localUri().path)
        if (oldPackage.exists())
            oldPackage.delete()

        val downloadManager = context?.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        DownloadReceiver.enqueue = downloadManager.enqueue(request)
    }

    suspend fun checkVersion(): Boolean {
        val path = File(
            context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            versionMeta
        ).path

        if (downloadVersion(path).await()) {
            val currentVersion = getCurrentVersion()
            try {
                val packageName = PackageName(getOnlinePackageName(path))
                return when {
                    currentVersion < packageName.version -> true
                    currentVersion > packageName.version -> {
                        Toast.makeText(
                            context,
                            "The current installed build number might be wrong",
                            Toast.LENGTH_LONG
                        ).show()
                        false
                    }
                    else -> {
                        Toast.makeText(
                            context,
                            "The latest Android image is already installed.",
                            Toast.LENGTH_LONG
                        ).show()
                        false
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Package name is wrong!",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        }
        return false
    }

    private fun localUri(): Uri {
        return Uri.parse("file://" + DOWNLOAD_DIR + "update.zip")
    }

    private fun getOnlinePackageName(path: String): String {
        val bufferReader = BufferedReader(FileReader(File(path)))
        val name = StringBuilder().append(bufferReader.readLine())
        packageName = name.toString()
        return this.packageName
    }

    private fun getCurrentVersion(): Int {
        val incremental = Build.VERSION.INCREMENTAL
        return incremental.toInt()
    }

    private fun downloadVersion(path: String): Deferred<Boolean> = CoroutineScope(Dispatchers.IO).async {
        File(path).delete()
        return@async try {
            URL(url + versionMeta).openStream().use { input ->
                FileOutputStream(File(path)).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (exceptoin:Exception) {
            Log.d(this.javaClass.name, exceptoin.toString())
            false
        }
    }
}
