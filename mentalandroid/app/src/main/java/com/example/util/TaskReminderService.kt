package com.example.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.model.Task
import java.util.Calendar
import java.util.Date

/**
 * 任务提醒服务，用于设置和管理任务提醒
 */
class TaskReminderService {
    companion object {
        private const val CHANNEL_ID = "task_reminder_channel"
        private const val NOTIFICATION_ID = 1001
        private const val REQUEST_CODE = 1002

        /**
         * 设置每日任务提醒
         */
        fun setDailyTaskReminder(context: Context) {
            // 创建通知渠道
            createNotificationChannel(context)

            // 设置每天上午9点提醒
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 9)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)

            // 如果当前时间已过今天的9点，则设置为明天的9点
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TaskReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 设置重复提醒
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }

        /**
         * 为未完成的任务设置额外提醒
         */
        fun setExtraReminder(context: Context, task: Task) {
            // 根据提醒次数设置不同的提醒频率
            val interval = when (task.reminderCount) {
                0 -> AlarmManager.INTERVAL_HOUR // 第一次额外提醒，1小时后
                1 -> AlarmManager.INTERVAL_HALF_HOUR // 第二次额外提醒，30分钟后
                else -> AlarmManager.INTERVAL_FIFTEEN_MINUTES // 后续提醒，15分钟后
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TaskReminderReceiver::class.java).apply {
                putExtra("TASK_ID", task.id)
                putExtra("EXTRA_REMINDER", true)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE + task.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + interval,
                pendingIntent
            )
        }

        /**
         * 创建通知渠道
         */
        private fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "任务提醒"
                val descriptionText = "每日任务提醒和未完成任务提醒"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                val notificationManager: NotificationManager = 
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        /**
         * 显示任务提醒通知
         */
        fun showTaskNotification(context: Context, task: Task) {
            val notificationManager: NotificationManager = 
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("任务提醒: ${task.title}")
                .setContentText(task.description.ifEmpty { "请完成此任务" })
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            notificationManager.notify(NOTIFICATION_ID + task.id, builder.build())
        }
    }

    /**
     * 任务提醒广播接收器
     */
    class TaskReminderReceiver : BroadcastReceiver() {
        @SuppressLint("UnsafeProtectedBroadcastReceiver")
        override fun onReceive(context: Context, intent: Intent) {
            val dbHelper = DatabaseHelper(context)
            val isExtraReminder = intent.getBooleanExtra("EXTRA_REMINDER", false)
            val taskId = intent.getIntExtra("TASK_ID", -1)

            if (isExtraReminder && taskId != -1) {
                // 额外提醒特定任务
                val task = dbHelper.getTaskById(taskId)
                if (task != null && !task.isCompleted) {
                    showTaskNotification(context, task)
                    // 更新提醒次数并设置下一次提醒
                    val updatedTask = task.copy(
                        reminderCount = task.reminderCount + 1,
                        lastReminderDate = Date()
                    )
                    dbHelper.updateTask(updatedTask)
                    setExtraReminder(context, updatedTask)
                }
            } else {
                // 每日提醒，检查所有未完成任务
                val tasks = dbHelper.getAllTasks().filter { !it.isCompleted }
                tasks.forEach { task ->
                    showTaskNotification(context, task)
                    // 检查是否需要提高提醒频率（前一天的任务）
                    val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                    val yesterday = calendar.time
                    if (task.createTime.before(yesterday)) {
                        setExtraReminder(context, task)
                    }
                }
            }
        }
    }
}