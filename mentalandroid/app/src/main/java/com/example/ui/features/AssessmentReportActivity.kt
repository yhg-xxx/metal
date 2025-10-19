package com.example.ui.features

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.ui.theme.MentalTheme

/**
 * 心理状态评估报告Activity
 * 用于显示测试后的心理状态评估报告
 */
class AssessmentReportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MentalTheme {
                AssessmentReportScreen()
            }
        }
    }
}