package com.example.model

import java.io.Serializable
import java.util.Date

/**
 * 任务数据类，用于存储计划清单的任务信息
 */
data class Task(
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: Int = 1, // 1: 低, 2: 中, 3: 高
    val createTime: Date = Date(),
    val reminderCount: Int = 0, // 提醒次数，用于提高前一天未完成任务的提醒频率
    val lastReminderDate: Date? = null // 最后提醒日期
) : Serializable