package com.example.util

import com.example.model.Counselor
import timber.log.Timber

/**
 * 咨询师相关的工具函数
 */
object CounselorUtils {
    
    // 解析擅长领域
    fun parseSpecialization(specialization: String?): String {
        if (specialization.isNullOrEmpty()) return "暂无擅长领域"
        
        return try {
            if (specialization.startsWith("[")) {
                val content = specialization.trim('[', ']')
                if (content.isNotEmpty()) {
                    val specializations = content.split(',')
                    specializations.joinToString("、") { it.trim('"', ' ') }
                } else {
                    "暂无擅长领域"
                }
            } else {
                specialization
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse specialization: $specialization")
            "暂无擅长领域"
        }
    }
    
    // 解析治疗流派
    fun parseTherapeuticApproach(approach: String?): String {
        if (approach.isNullOrEmpty()) return "未提供"
        
        return try {
            if (approach.startsWith("[")) {
                val content = approach.trim('[', ']')
                if (content.isNotEmpty()) {
                    val approaches = content.split(',')
                    approaches.joinToString("、") { it.trim('"', ' ') }
                } else {
                    "未提供"
                }
            } else {
                approach
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse therapeutic approach: $approach")
            "未提供"
        }
    }
    
    // 解析服务类型
    fun parseServiceTypes(serviceTypes: String?): String {
        if (serviceTypes.isNullOrEmpty()) return "未指定"
        
        return try {
            if (serviceTypes.startsWith("[")) {
                val content = serviceTypes.trim('[', ']')
                if (content.isNotEmpty()) {
                    val types = content.split(',')
                    // 转换英文类型为中文显示
                    types.joinToString("、") { type ->
                        val trimmed = type.trim('"', ' ')
                        when (trimmed) {
                            "VOICE" -> "语音"
                            "VIDEO" -> "视频"
                            "TEXT" -> "文字"
                            else -> trimmed
                        }
                    }
                } else {
                    "未指定"
                }
            } else {
                serviceTypes
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse service types: $serviceTypes")
            "未指定"
        }
    }
    
    // 解析可用日期
    fun parseAvailableDays(days: String?): String {
        if (days.isNullOrEmpty()) return "未指定"
        
        return try {
            if (days.startsWith("[")) {
                val content = days.trim('[', ']')
                if (content.isNotEmpty()) {
                    val weekdays = content.split(',')
                    // 转换英文星期为中文显示
                    weekdays.joinToString("、") { day ->
                        val trimmed = day.trim('"', ' ')
                        when (trimmed) {
                            "MONDAY" -> "周一"
                            "TUESDAY" -> "周二"
                            "WEDNESDAY" -> "周三"
                            "THURSDAY" -> "周四"
                            "FRIDAY" -> "周五"
                            "SATURDAY" -> "周六"
                            "SUNDAY" -> "周日"
                            else -> trimmed
                        }
                    }
                } else {
                    "未指定"
                }
            } else {
                days
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse available days: $days")
            "未指定"
        }
    }
    
    // 解析工作时间
    fun parseWorkingHours(hours: String?): String {
        if (hours.isNullOrEmpty()) return "未指定"
        
        return try {
            if (hours.startsWith("{")) {
                // 使用简单的字符串操作提取时间，格式如：{"end":"18:00","start":"09:00"}
                val startTime = extractTimeByKey(hours, "start")
                val endTime = extractTimeByKey(hours, "end")
                
                if (startTime != null && endTime != null) {
                    "$startTime - $endTime"
                } else {
                    "未指定"
                }
            } else {
                hours
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse working hours: $hours")
            "未指定"
        }
    }
    
    // 从JSON字符串中提取指定键的时间值（使用简单的字符串操作）
    private fun extractTimeByKey(jsonStr: String, key: String): String? {
        try {
            // 查找键值对，如 "start":"09:00"
            val keyWithQuote = '"' + key + '"'
            val colonIndex = jsonStr.indexOf(keyWithQuote) + keyWithQuote.length
            if (colonIndex > keyWithQuote.length) {
                // 找到键后的冒号位置
                val startQuoteIndex = jsonStr.indexOf('"', colonIndex + 1)
                if (startQuoteIndex > colonIndex) {
                    // 找到值的开始引号
                    val endQuoteIndex = jsonStr.indexOf('"', startQuoteIndex + 1)
                    if (endQuoteIndex > startQuoteIndex) {
                        return jsonStr.substring(startQuoteIndex + 1, endQuoteIndex)
                    }
                }
            }
            return null
        } catch (e: Exception) {
            Timber.e(e, "Failed to extract $key from $jsonStr")
            return null
        }
    }
    
    // 解析咨询时长
    fun parseSessionDurations(durations: Any?): String {
        if (durations == null) return "未指定"
        
        return try {
            val durationStr = durations.toString()
            if (durationStr.startsWith("[")) {
                val content = durationStr.trim('[', ']')
                if (content.isNotEmpty()) {
                    val durationList = content.split(',')
                    durationList.joinToString("、") { it.trim() }
                } else {
                    "未指定"
                }
            } else {
                durationStr
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse session durations: $durations")
            "未指定"
        }
    }
    
    // 获取资质标签
    fun getQualificationLabel(counselor: Counselor): String {
        return if (counselor.counselorStatus == "APPROVED") {
            "国家二级心理咨询师"
        } else {
            "心理咨询师"
        }
    }
    
    // 获取服务标签
    fun getServiceLabels(counselor: Counselor): String {
        val labels = mutableListOf<String>()
        
        if (counselor.counselorStatus == "APPROVED") {
            labels.add("平台优选")
        }
        
        if (counselor.serviceTypes != null && counselor.serviceTypes.contains("视频")) {
            labels.add("可视频")
        }
        
        return labels.joinToString(" ")
    }
}