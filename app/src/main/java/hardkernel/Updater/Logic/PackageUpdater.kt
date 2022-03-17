package hardkernel.Updater.Logic

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.RecoverySystem
import android.widget.Toast
import hardkernel.Updater.NotifyManager
import hardkernel.Updater.Service.UpdateService
import java.io.File
import java.io.IOException

class PackageUpdater(private val context: Context) {
    private lateinit var remoteUpdater: RemoteUpdater

    fun getPackageFromLocal(uri: Uri): File {
        return LocalLoader(context, uri).file
    }

    suspend fun requestPackageFrom(url: String) {
        remoteUpdater = RemoteUpdater(context, url)
        remoteUpdater.requestPackage()
    }

    fun requestDownloadPackage() {
        remoteUpdater.requestDownload()
    }

    @Throws(IOException::class)
    fun checkFile(file: File) {
        try {
            val validatePath = file.path.replace("/data/media/0/", "/sdcard/")
            RecoverySystem.verifyPackage(File(validatePath), null, null)
        } catch (e: Exception) {
            Toast.makeText(context,
                "The package file seems to be corrupted!!\n" +
                        "Please select another package file ..",
                Toast.LENGTH_LONG).show()
            throw IOException (e)
        }

        val installIntent = Intent(context, UpdateService::class.java)
        installIntent.action = UpdateService.CMD.INSTALL.name
        installIntent.putExtra(UpdateService.CMD.INSTALL.name, file)

        NotifyManager.createChannel(context)

        NotifyManager.buildNNotify(context,
        "Updater", "start Update", "Update",
            installIntent)
    }

    fun copyConfig() {
        try {
            val config = File("/fat/config.ini")
            config.copyTo(File("/storage/emulated/0/.config.ini.backup"), true)
        } catch (e : Exception) {
            Toast.makeText(context,
                "Backup the config.ini file is failed",
                Toast.LENGTH_LONG).show()
            throw IOException (e)
        }
    }

    @Throws(IOException::class)
    fun install (file: File) {
        try {
            copyConfig()
            RecoverySystem.installPackage(context, file)
        } catch (e :Exception) {
            Toast.makeText(
                context,
                "Error while install OTA package: $e",
            Toast.LENGTH_LONG).show()
            throw IOException (e)
        }
    }
}
