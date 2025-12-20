package com.example.ui.features


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MentalTheme
import com.example.util.DatabaseHelper

/**
 * 测试题页面Activity
 */
class TestQuestionsActivity : ComponentActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DatabaseHelper(this)

        // 从Intent获取学习包ID和标题
        val learningPackageId = intent.getLongExtra("learningPackageId", 0)
        val learningPackageTitle = intent.getStringExtra("learningPackageTitle") ?: "测试题"

        setContent {
            MentalTheme {
                // 表面容器
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TestQuestionsScreen(
                        learningPackageId = learningPackageId,
                        learningPackageTitle = learningPackageTitle,
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

/**
 * 测试题页面的主要内容Composable组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestQuestionsScreen(
    learningPackageId: Long,
    learningPackageTitle: String,
    onBackClick: () -> Unit
) {
    // 获取测试题数据
    val questions by remember {
        mutableStateOf(getMockQuestions(learningPackageId))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = learningPackageTitle,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            )
        }
    ) {
        // 测试题内容区域
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
        ) {
            // 题目列表
            questions.forEachIndexed { index, question ->
                QuestionCard(question = question, questionNumber = index + 1)
                if (index < questions.size - 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
 * 题目卡片组件
 */
@Composable
fun QuestionCard(question: TestQuestion, questionNumber: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 题号和题目
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "${questionNumber}. ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = question.questionText,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 选项列表
            question.options.forEachIndexed { optionIndex, option ->
                val optionLetter = (65 + optionIndex).toChar() // A, B, C, D
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = false, // 这里应该绑定到实际的选择状态
                        onClick = { /* 处理选项选择 */ }
                    )
                    Text(
                        text = "${optionLetter}. $option",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * 获取模拟测试题数据
 */
private fun getMockQuestions(learningPackageId: Long): List<TestQuestion> {
    // 返回模拟数据
    return listOf(
        TestQuestion(
            id = 1,
            learningPackageId = learningPackageId,
            questionText = "以下关于心理健康的说法，哪项是正确的？",
            options = listOf(
                "心理健康意味着没有任何烦恼",
                "心理健康是一个动态的过程",
                "心理健康的人不会有情绪波动",
                "心理健康只与心理因素有关"
            ),
            correctAnswer = "B"
        ),
        TestQuestion(
            id = 2,
            learningPackageId = learningPackageId,
            questionText = "应对压力的有效方法不包括以下哪项？",
            options = listOf(
                "深呼吸和冥想",
                "保持规律的运动",
                "过度工作以忘记压力",
                "与亲友交流"
            ),
            correctAnswer = "C"
        )
    )
}

/**
 * 测试题数据类
 */
data class TestQuestion(
    val id: Long,
    val learningPackageId: Long,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String
)