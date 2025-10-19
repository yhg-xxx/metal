package com.example.ui.features

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.MentalTheme

// 题目类型枚举
enum class QuestionType {
    SINGLE_CHOICE,
    MULTIPLE_CHOICE,
    TRUE_FALSE
}

// 测试题目数据类
data class TestQuestion(
    val id: Int,
    val type: QuestionType,
    val question: String,
    val options: List<String>,
    val correctAnswers: List<Int>,
    val explanation: String
)

/**
 * 测试题页面组件
 * 包含题目展示、答题、时间限制和成绩显示功能
 */
@Composable
fun TestQuestionsScreen(modifier: Modifier = Modifier) {
    // 测试数据
    val questions = remember {
        listOf(
            TestQuestion(
                id = 1,
                type = QuestionType.SINGLE_CHOICE,
                question = "正念冥想的主要目的是什么？",
                options = listOf("获得超能力", "放松身心，缓解压力", "提高睡眠质量", "增强记忆力"),
                correctAnswers = listOf(1),
                explanation = "正念冥想的主要目的是帮助人们放松身心，缓解压力，提高专注力和情绪调节能力。"
            ),
            TestQuestion(
                id = 2,
                type = QuestionType.MULTIPLE_CHOICE,
                question = "以下哪些是缓解压力的有效方法？（可多选）",
                options = listOf("深呼吸练习", "渐进式肌肉放松", "长时间熬夜", "冥想"),
                correctAnswers = listOf(0, 1, 3),
                explanation = "深呼吸练习、渐进式肌肉放松和冥想都是有效的压力缓解方法，而长时间熬夜会增加压力。"
            ),
            TestQuestion(
                id = 3,
                type = QuestionType.TRUE_FALSE,
                question = "冥想可以改善睡眠质量。",
                options = listOf("正确", "错误"),
                correctAnswers = listOf(0),
                explanation = "研究表明，定期冥想可以帮助改善睡眠质量，减少失眠问题。"
            )
        )
    }
    
    // 答题状态
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf(-1) }
    var selectedMultipleOptions by remember { mutableStateOf(listOf<Int>()) }
    var showTimeUpDialog by remember { mutableStateOf(false) }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableStateOf(15 * 60) } // 15分钟，单位：秒
    var isTestCompleted by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var wrongQuestions by remember { mutableStateOf(listOf<TestQuestion>()) }
    
    // 格式化时间显示
    val formattedTime = remember(timeRemaining) {
        val minutes = timeRemaining / 60
        val seconds = timeRemaining % 60
        "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }
    
    // 处理题目切换
    val handleNextQuestion = {
        // 保存当前答案并切换到下一题
        if (currentQuestionIndex < questions.size - 1) {
            currentQuestionIndex++
            // 重置当前题目的选择状态
            selectedOption = -1
            selectedMultipleOptions = listOf()
        }
    }
    
    val handlePreviousQuestion = {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--
            // 可以在这里恢复之前的选择状态（如果需要）
        }
    }
    
    // 提交测试
    val handleSubmitTest = {
        // 计算成绩
        score = 0
        wrongQuestions = listOf()
        
        // 这里简化了成绩计算，实际应该根据所有题目答案计算
        // 由于是假数据，我们假设用户答对了2题
        score = 2
        wrongQuestions = questions.takeLast(1)
        
        isTestCompleted = true
    }
    
    // 显示答题页面或成绩页面
    if (isTestCompleted) {
        // 成绩页面
        ScorePage(
            score = score,
            totalQuestions = questions.size,
            wrongQuestions = wrongQuestions,
            onBack = { /* 返回操作 */ }
        )
    } else {
        // 答题页面
        Column(modifier = modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
            // 顶部导航栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.White)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* 返回按钮点击事件 */ }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "返回")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "学习包测试",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(
                            if (timeRemaining < 300) Color(0xFFFFCCCC) else Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {
                    Text(
                        text = formattedTime,
                        color = if (timeRemaining < 300) Color.Red else Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            // 题目指示器
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in questions.indices) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (i == currentQuestionIndex) Color(0xFF5A67D8) else Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { 
                                currentQuestionIndex = i
                                selectedOption = -1
                                selectedMultipleOptions = listOf()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${i + 1}",
                            color = if (i == currentQuestionIndex) Color.White else Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // 题目内容
            val currentQuestion = questions[currentQuestionIndex]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                // 题目类型和序号
                Text(
                    text = "第 ${currentQuestion.id} 题 - ${getQuestionTypeName(currentQuestion.type)}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // 题目内容
                Text(
                    text = currentQuestion.question,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // 选项
                when (currentQuestion.type) {
                    QuestionType.SINGLE_CHOICE -> {
                        currentQuestion.options.forEachIndexed { index, option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable { selectedOption = index },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedOption == index,
                                    onClick = { selectedOption = index }
                                )
                                Text(
                                    text = option,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                    QuestionType.MULTIPLE_CHOICE -> {
                        currentQuestion.options.forEachIndexed { index, option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable {
                                        if (selectedMultipleOptions.contains(index)) {
                                            selectedMultipleOptions = selectedMultipleOptions.filter { it != index }
                                        } else {
                                            selectedMultipleOptions = selectedMultipleOptions + index
                                        }
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedMultipleOptions.contains(index),
                                    onCheckedChange = {
                                        if (selectedMultipleOptions.contains(index)) {
                                            selectedMultipleOptions = selectedMultipleOptions.filter { it != index }
                                        } else {
                                            selectedMultipleOptions = selectedMultipleOptions + index
                                        }
                                    }
                                )
                                Text(
                                    text = option,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                    QuestionType.TRUE_FALSE -> {
                        currentQuestion.options.forEachIndexed { index, option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable { selectedOption = index },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedOption == index,
                                    onClick = { selectedOption = index }
                                )
                                Text(
                                    text = option,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
                
                // 底部按钮
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = handlePreviousQuestion,
                        enabled = currentQuestionIndex > 0,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Text(text = "上一题")
                    }
                    
                    if (currentQuestionIndex < questions.size - 1) {
                        Button(
                            onClick = handleNextQuestion,
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        ) {
                            Text(text = "下一题")
                        }
                    } else {
                        Button(
                            onClick = { showSubmitDialog = true },
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        ) {
                            Text(text = "提交测试")
                        }
                    }
                }
            }
        }
    }
    
    // 时间到对话框
    if (showTimeUpDialog) {
        AlertDialog(
            onDismissRequest = { showTimeUpDialog = false },
            title = { Text(text = "时间到！") },
            text = { Text(text = "答题时间已结束，将自动提交您的答案。") },
            confirmButton = {
                Button(onClick = {
                    showTimeUpDialog = false
                    handleSubmitTest()
                }) {
                    Text(text = "确定")
                }
            },
            properties = DialogProperties()
        )
    }
    
    // 提交确认对话框
    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text(text = "确认提交") },
            text = { Text(text = "确定要提交测试吗？提交后将无法修改答案。") },
            confirmButton = {
                Button(onClick = {
                    showSubmitDialog = false
                    handleSubmitTest()
                }) {
                    Text(text = "确定提交")
                }
            },
            dismissButton = {
                Button(onClick = { showSubmitDialog = false }) {
                    Text(text = "取消")
                }
            },
            properties = DialogProperties()
        )
    }
}

/**
 * 获取题目类型名称
 */
fun getQuestionTypeName(type: QuestionType): String {
    return when (type) {
        QuestionType.SINGLE_CHOICE -> "单选题"
        QuestionType.MULTIPLE_CHOICE -> "多选题"
        QuestionType.TRUE_FALSE -> "判断题"
    }
}

/**
 * 成绩页面组件
 */
@Composable
fun ScorePage(
    score: Int,
    totalQuestions: Int,
    wrongQuestions: List<TestQuestion>,
    onBack: () -> Unit
) {
    val percentage = (score.toFloat() / totalQuestions * 100).toInt()
    
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        // 顶部导航栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.White)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "返回")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "测试结果",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // 成绩卡片
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.White)
                .clip(RoundedCornerShape(16.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "得分",
                    fontSize = 18.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "${score}/${totalQuestions}",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A67D8),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "${percentage}%",
                    fontSize = 24.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 评价文字
                val evaluationText = when {
                    percentage >= 90 -> "太棒了！你对知识点掌握得非常好。"
                    percentage >= 70 -> "不错！继续努力。"
                    percentage >= 60 -> "基本掌握，但还有提升空间。"
                    else -> "需要更多练习来巩固知识点。"
                }
                
                Text(
                    text = evaluationText,
                    fontSize = 16.sp,
                    color = Color(0xFF4A5568)
                )
            }
        }
        
        // 错题解析
        if (wrongQuestions.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(
                    text = "错题解析",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                wrongQuestions.forEach { question ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .clip(RoundedCornerShape(8.dp))
                            .padding(16.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        Column {
                            Text(
                                text = question.question,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Text(
                                text = "正确答案：",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            
                            question.correctAnswers.forEach { index ->
                                Text(
                                    text = "- ${question.options[index]}",
                                    fontSize = 14.sp,
                                    color = Color.Green,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            
                            Text(
                                text = "解析：",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = question.explanation,
                                fontSize = 14.sp,
                                color = Color(0xFF4A5568)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TestQuestionsScreenPreview() {
    MentalTheme {
        TestQuestionsScreen()
    }
}