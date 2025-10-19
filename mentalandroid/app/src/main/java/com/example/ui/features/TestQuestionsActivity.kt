package com.example.ui.features

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.ui.theme.MentalTheme

/**
 * 测试题页面Activity
 * 用于显示学习包配套的测试题
 */
class TestQuestionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MentalTheme {
                TestQuestionsScreen()
            }
        }
    }
}