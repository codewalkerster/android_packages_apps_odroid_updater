package hardkernel.Updater.Service

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import hardkernel.Updater.Logic.PackageUpdater
import hardkernel.Updater.NotifyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class UpdateService : Service() {
    enum class CMD{
        LOCAL_UPDATE_START,
        REMOTE_UPDATE_START,
        REMOTE_UPDATE_DOWNLOAD,
        REMOTE_INSTALL,
        INSTALL
    }

    private lateinit var packageUpdater: PackageUpdater

    override fun onCreate() {
        Log.d(this.javaClass.name, "start service")

        packageUpdater = PackageUpdater(baseContext)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            when (intent?.action) {
                CMD.LOCAL_UPDATE_START.name -> {
                    val uri = intent.getParcelableExtra<Uri>(CMD.LOCAL_UPDATE_START.name)
                    Log.d(this.packageName, "local - " + uri.toString())
                    localUpdate(uri)
                }
                CMD.REMOTE_UPDATE_START.name -> {
                    val url = intent.getStringExtra(CMD.REMOTE_UPDATE_START.name)
                    Log.d(this.packageName, "remote - " + url!!)
                    remoteUpdateStart(url)
                }
                CMD.REMOTE_UPDATE_DOWNLOAD.name -> {
                    remoteDownload()
                    NotifyManager.closeNotify(baseContext)
                }
                CMD.INSTALL.name -> {
                    val file = intent.getSerializableExtra(CMD.INSTALL.name) as File
                    packageUpdater.install(file)
                }
                CMD.REMOTE_INSTALL.name -> {
                    val file = intent.getSerializableExtra(CMD.REMOTE_INSTALL.name) as File
                    remoteInstall(file)
                }
            }
        } catch (e: Exception) {
            Log.e(this.javaClass.name, "error - $e")
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @Throws(IOException::class)
    private fun localUpdate(uri: Uri?) {
        try {
            val packageFile = uri?.let { packageUpdater.getPackageFromLocal(it) }
            packageFile?.let { packageUpdater.checkFile(packageFile) }
        } catch (e:IOException) {
            throw IOException(e)
        }
    }

    @Throws(IOException::class)
    private fun remoteUpdateStart(url: String?) {
        if (url != null) let {
            CoroutineScope(Dispatchers.Main).launch {
                packageUpdater.requestPackageFrom(url)
            }
        } else {
            throw IOException("URL is NULL")
        }
    }

    private fun remoteDownload() {
        packageUpdater.requestDownloadPackage()
    }

    @Throws(IOException::class)
    private fun remoteInstall(file: File) {
        var target:File
        target = if (file.path.startsWith("/storage/emulated/0")) {
            File(file.path.replace("/storage/emulated/", "/data/media/"))
        } else
            file
        packageUpdater.checkFile(target)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
