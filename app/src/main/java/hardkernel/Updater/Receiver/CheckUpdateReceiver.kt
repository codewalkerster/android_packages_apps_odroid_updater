package hardkernel.Updater.Receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import hardkernel.Updater.Logic.ServerManager
import hardkernel.Updater.Service.UpdateService

class CheckUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (ServerManager.isCheckAtBoot()) {
                val requestUpdate = Intent(context, UpdateService::class.java)
                requestUpdate.action = UpdateService.CMD.REMOTE_UPDATE_START.name
                requestUpdate.putExtra(
                    UpdateService.CMD.REMOTE_UPDATE_START.name,
                    ServerManager.getURL()
                )
                context.startForegroundService(requestUpdate)
            }
        }
    }
}