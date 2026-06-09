package com.zui.perfctl

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.Settings

class PerfCtlQuickService : Service() {
    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            PerfCtlContract.ACTION_SET_60 -> PerfCtlRequest.send(this, PerfCtlContract.CMD_SET_REFRESH, rate = 60)
            PerfCtlContract.ACTION_SET_90 -> PerfCtlRequest.send(this, PerfCtlContract.CMD_SET_REFRESH, rate = 90)
            PerfCtlContract.ACTION_SET_120 -> PerfCtlRequest.send(this, PerfCtlContract.CMD_SET_REFRESH, rate = 120)
            PerfCtlContract.ACTION_SET_144 -> PerfCtlRequest.send(this, PerfCtlContract.CMD_SET_REFRESH, rate = 144)
            PerfCtlContract.ACTION_RESTORE -> PerfCtlRequest.send(this, PerfCtlContract.CMD_RESTORE_REFRESH)
            PerfCtlContract.ACTION_AUTO_ON -> PerfCtlRequest.send(this, PerfCtlContract.CMD_ENABLE_AUTO_REFRESH)
            PerfCtlContract.ACTION_AUTO_OFF -> PerfCtlRequest.send(this, PerfCtlContract.CMD_DISABLE_AUTO_REFRESH)
            else -> Unit
        }
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            1,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }
        builder
            .setSmallIcon(R.drawable.ic_stat_perfctl)
            .setContentTitle("ZuiperfCtl")
            .setContentText(notificationText())
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setShowWhen(false)
            .setLocalOnly(true)
            .setDefaults(0)
            .addAction(action("60", PerfCtlContract.ACTION_SET_60, 60))
            .addAction(action("90", PerfCtlContract.ACTION_SET_90, 90))
            .addAction(action("120", PerfCtlContract.ACTION_SET_120, 120))
            .addAction(action("144", PerfCtlContract.ACTION_SET_144, 144))
            .addAction(action("默认", PerfCtlContract.ACTION_RESTORE, 200))
        return builder.build()
    }

    private fun notificationText(): String {
        val peak = Settings.System.getString(contentResolver, "peak_refresh_rate")
            ?.takeIf { it != "null" }
            ?.removeSuffix(".0")
        val auto = Settings.System.getString(contentResolver, PerfCtlContract.KEY_AUTO_REFRESH) == "1"
        val refresh = peak?.let { "${it}Hz" } ?: "系统默认"
        return "刷新率 $refresh / 自动${if (auto) "开" else "关"}"
    }

    private fun action(title: String, action: String, requestCode: Int): Notification.Action {
        val intent = PendingIntent.getService(
            this,
            requestCode,
            Intent(this, PerfCtlQuickService::class.java).setAction(action),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return Notification.Action.Builder(R.drawable.ic_stat_perfctl, title, intent).build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "ZuiperfCtl 静音控制",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "刷新率快捷控制"
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "zui_perfctl_quick"
        private const val NOTIFICATION_ID = 18701

        fun start(context: Context) {
            val intent = Intent(context, PerfCtlQuickService::class.java)
                .setAction(PerfCtlContract.ACTION_REFRESH_NOTIFICATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
