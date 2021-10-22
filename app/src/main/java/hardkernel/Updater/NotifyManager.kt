package hardkernel.Updater

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import hardkernel.Updater.Logic.SysProperty

object NotifyManager {
    private const val Channel_id = "UpdateNotification"
    private const val Notification_id = 0x202111

    fun createChannel(context: Context) {
        val channel = NotificationChannel(Channel_id,
            Channel_id, NotificationManager.IMPORTANCE_HIGH)
        channel.description = "Update Notification Channel."
        val manager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun buildNNotify(
        context: Context, contentTitle: String, contentMsg: String,
        actionTitle: String, intent: Intent
    ) {
        val kioskMode = SysProperty.get("persist.kiosk_mode", false)
        if (kioskMode) {
            val builder = AlertDialog.Builder(context)
            val view = View.inflate(context, R.layout.update_dialog, null)
            val alertTitle = view.findViewById(R.id.update_dialog_title) as TextView
            alertTitle.text = contentTitle
            val alertMsg = view.findViewById(R.id.update_dialog_message) as TextView
            val alert: AlertDialog = builder.create()
            val timer = object : CountDownTimer(10000, 1000) {
                override fun onTick(milliseconds: Long) {
                    alertMsg.text = contentMsg + "(" + milliseconds / 1000 + ")"
                }

                override fun onFinish() {
                    if (alert.isShowing) {
                        alert.dismiss()
                    }
                }
            }
            val actionBtn = view.findViewById(R.id.update_dialog_ok) as TextView
            actionBtn.setOnClickListener() {
                context.startService(intent)
                alert.dismiss()
                timer.cancel()
            }
            actionBtn.text = actionTitle
            alert.setView(view)
            alert.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY - 1)
            alert.show()
            timer.start()
            val alertWindow = alert.window
            alertWindow?.setGravity(Gravity.CENTER)
        } else {
            val actionIntent = PendingIntent.getService(
                context, 0, intent, 0
            )
            val builder = NotificationCompat.Builder(context, Channel_id)
                .setSmallIcon(R.drawable.ic_system_update)
                .setContentTitle(contentTitle)
                .setStyle(NotificationCompat.BigTextStyle().bigText(contentMsg))
                .setContentText(contentMsg)
                .addAction(R.drawable.ic_system_update, actionTitle, actionIntent)
                .setAutoCancel(true)
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(Notification_id, builder.build())
        }
    }

    fun closeNotify(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(Notification_id)
    }
}
