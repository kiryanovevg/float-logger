package com.roonyx.pointrlogger

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val mFloatViewManager by lazy {
        FloatViewManager(this)
    }
    
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (checkDrawOverlayPermission() && !mFloatViewManager.isFloatViewShowing) {
                mFloatViewManager.showFloatView()
            }
        }
    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.w(TAG, "onCreate")

        val clickListener: (View) -> Unit = {
            if (it is AppCompatButton) {
                Log.w(TAG, "Clicked #${it.text}")
            }
        }

        btn1.setOnClickListener(clickListener)
        btn2.setOnClickListener(clickListener)
        btn3.setOnClickListener(clickListener)
        btn4.setOnClickListener(clickListener)

        root.setOnClickListener {
            Log.w(TAG, "Clicked!!!")
        }

        btnSend.setOnClickListener {
            val text = et.text.trim().toString()
            if (text.isNotEmpty()) {
                val div = "------------------------"
                Log.w(TAG, div)
                Log.w(TAG, text)
                Log.w(TAG, div)

                et.text = null
            }
        }

        registerReceiver(broadcastReceiver, IntentFilter(BROADCAST_ACTION))
        createNotification()
    }

    private fun createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )

            notificationManager.createNotificationChannel(notificationChannel)
        }

        val resultPendingIntent = PendingIntent.getBroadcast(
            this, 1,
            Intent(BROADCAST_ACTION),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat
            .Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("DEBUG")
            .setContentText("Click to show LOGS")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(resultPendingIntent)
            .setOngoing(true)

        val notification: Notification = builder.build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onResume() {
        Log.w(TAG, "onResume")
        super.onResume()
    }

    override fun onPause() {
        Log.w(TAG, "onPause")
        super.onPause()
    }

    override fun onDestroy() {
        Log.w(TAG, "onDestroy")
        super.onDestroy()

        unregisterReceiver(broadcastReceiver)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID)
        }
        notificationManager.cancel(NOTIFICATION_ID)

        if (mFloatViewManager.isFloatViewShowing)
            mFloatViewManager.dismissFloatView()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_DRAW_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                mFloatViewManager.showFloatView()
            }
        }
    }

    private fun checkDrawOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, REQUEST_CODE_DRAW_OVERLAY_PERMISSION)
            false
        } else {
            true
        }
    }

    companion object {
        private const val REQUEST_CODE_DRAW_OVERLAY_PERMISSION = 5
        private const val TAG = "LOGGER"
        private const val BROADCAST_ACTION = "BroadcastActionFilter"
        private const val NOTIFICATION_ID = 777
        private const val NOTIFICATION_CHANNEL_ID = "777"
        private const val NOTIFICATION_CHANNEL_NAME = "OpenLogChannel"
    }
}
