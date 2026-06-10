package com.zui.perfctl

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.widget.RemoteViews

class PerfCtlQuickService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var notificationManager: NotificationManager
    private var pendingRate: Int? = null
    private var pendingRateUntilMs = 0L

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val rate = when (intent?.action) {
            PerfCtlContract.ACTION_SET_60 -> 60
            PerfCtlContract.ACTION_SET_90 -> 90
            PerfCtlContract.ACTION_SET_120 -> 120
            PerfCtlContract.ACTION_SET_144 -> 144
            PerfCtlContract.ACTION_SET_165 -> 165
            else -> intent?.getIntExtra(PerfCtlContract.EXTRA_RATE, 0)?.takeIf {
                it in PerfCtlContract.rates
            }
        }
        if (rate != null) {
            pendingRate = rate
            pendingRateUntilMs = SystemClock.elapsedRealtime() + PENDING_RATE_TIMEOUT_MS
            RefreshSceneController.lockRefresh(this, rate)
            PerfCtlRequest.send(
                this,
                PerfCtlContract.CMD_LEARN_REFRESH,
                rate = rate,
                pkg = RefreshSceneController.currentScenePackage(this),
            )
            handler.removeCallbacksAndMessages(null)
            updateNotification(rate)
            handler.postDelayed({ updateNotification() }, PENDING_RATE_TIMEOUT_MS + 80L)
        } else {
            updateNotification()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun updateNotification(rateOverride: Int? = null) {
        notificationManager.notify(NOTIFICATION_ID, buildNotification(rateOverride))
    }

    private fun buildNotification(rateOverride: Int? = null): Notification {
        val selectedRate = rateOverride ?: pendingDisplayRate() ?: currentRate()
        val content = RemoteViews(packageName, R.layout.notification_perfctl).apply {
            setTextViewText(R.id.notification_title, "ZuiperfCtl")
            bindRate(this, R.id.rate_60, 60, selectedRate, PerfCtlContract.ACTION_SET_60)
            bindRate(this, R.id.rate_90, 90, selectedRate, PerfCtlContract.ACTION_SET_90)
            bindRate(this, R.id.rate_120, 120, selectedRate, PerfCtlContract.ACTION_SET_120)
            bindRate(this, R.id.rate_144, 144, selectedRate, PerfCtlContract.ACTION_SET_144)
            bindRate(this, R.id.rate_165, 165, selectedRate, PerfCtlContract.ACTION_SET_165)
        }
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
            .setContentText("${selectedRate}Hz")
            .setContentIntent(openIntent)
            .setCustomContentView(content)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setLocalOnly(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setPriority(Notification.PRIORITY_LOW)
            .setDefaults(0)
            .setSound(null)
            .setVibrate(null)
        return builder.build()
    }

    private fun pendingDisplayRate(): Int? {
        val rate = pendingRate ?: return null
        if (SystemClock.elapsedRealtime() <= pendingRateUntilMs) {
            return rate
        }
        pendingRate = null
        pendingRateUntilMs = 0L
        return null
    }

    private fun bindRate(
        views: RemoteViews,
        viewId: Int,
        rate: Int,
        selectedRate: Int,
        action: String,
    ) {
        val selected = rate == selectedRate
        views.setTextViewText(viewId, rate.toString())
        views.setInt(
            viewId,
            "setBackgroundResource",
            if (selected) R.drawable.notify_rate_selected else R.drawable.notify_rate_normal,
        )
        views.setTextColor(viewId, if (selected) COLOR_SELECTED_TEXT else COLOR_NORMAL_TEXT)
        views.setOnClickPendingIntent(
            viewId,
            PendingIntent.getService(
                this,
                rate,
                Intent(this, PerfCtlQuickService::class.java)
                    .setAction(action)
                    .putExtra(PerfCtlContract.EXTRA_RATE, rate),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            ),
        )
    }

    private fun currentRate(): Int {
        return RefreshSceneController.currentRate(this)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val channel = NotificationChannel(
            CHANNEL_ID,
            "ZuiperfCtl refresh",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Per-app refresh quick controls"
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
        }
        notificationManager.deleteNotificationChannel("zui_perfctl_quick_v3")
        notificationManager.deleteNotificationChannel("zui_perfctl_quick_v4")
        notificationManager.deleteNotificationChannel("zui_perfctl_quick_v5")
        notificationManager.deleteNotificationChannel("zui_perfctl_quick_v6")
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "zui_perfctl_quick_v7"
        private const val NOTIFICATION_ID = 18701
        private const val PENDING_RATE_TIMEOUT_MS = 1600L
        private const val COLOR_SELECTED_TEXT = 0xFFFFFFFF.toInt()
        private const val COLOR_NORMAL_TEXT = 0xFF1C222A.toInt()

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
