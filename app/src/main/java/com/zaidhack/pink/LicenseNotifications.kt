package com.zaidhack.pink

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

/** Informational notifications. Root watchdog remains independent of app UI. */
object LicenseNotifications {
    private const val CHANNEL = "zh_license_status"
    private const val ID = 4214

    fun permitted(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun show(context: Context, title: String, detail: String) {
        if (!permitted(context)) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, "Zaid Hack • licencia", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Estado del overlay y avisos de vencimiento"
            }
        )
        val intent = Intent(context, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle(title)
            .setContentText(detail)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        manager.notify(ID, notification)
    }
}
