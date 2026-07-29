package com.merakhata.app.domain.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.merakhata.app.MainActivity
import com.merakhata.app.domain.accounting.AccountingEngine

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "mera_khata_reminders"
        const val CHANNEL_NAME = "Payment Reminders"
        const val EXTRA_CUSTOMER_ID = "extra_customer_id"
        const val EXTRA_CUSTOMER_NAME = "extra_customer_name"
        const val EXTRA_AMOUNT_MINOR = "extra_amount_minor"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val customerId = intent.getLongExtra(EXTRA_CUSTOMER_ID, -1L)
        val customerName = intent.getStringExtra(EXTRA_CUSTOMER_NAME) ?: "Customer"
        val amountMinor = intent.getLongExtra(EXTRA_AMOUNT_MINOR, 0L)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for payment reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_CUSTOMER_ID, customerId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            customerId.toInt(),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val amountStr = AccountingEngine.formatCurrency(amountMinor)
        val notificationText = "Payment reminder for $customerName ($amountStr pending)"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Mera Khata Payment Reminder")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(customerId.toInt(), notification)
    }
}
