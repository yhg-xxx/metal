package com.example.ui.features

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MentalTheme

/**
 * 心理状态评估报告屏幕组件
 * 包含学习效果、状态评估和建议
 */
@Composable
fun AssessmentReportScreen(modifier: Modifier = Modifier) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    
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
                text = "心理状态评估报告",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showSaveDialog = true }) {
                Text(
                    text = "保存",
                    fontSize = 16.sp,
                    color = Color(0xFF5A67D8)
                )
            }
            IconButton(onClick = { showShareDialog = true }) {
                Icon(imageVector = Icons.Filled.Share, contentDescription = "分享")
            }
        }
        
        // 报告内容滚动区域
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // 报告头部信息
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
                        text = "正念冥想学习评估报告",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "评估日期：2023-10-15",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
            
            // 学习效果部分
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(top = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "学习效果",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // 知识点掌握情况
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Text(
                            text = "知识点掌握情况",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // 正确率进度条
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "整体正确率", fontSize = 14.sp)
                                Text(text = "80%", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .background(Color(0xFFE0E0E0))
                                    .clip(RoundedCornerShape(4.dp))
                                    .padding(1.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .height(6.dp)
                                        .background(Color(0xFF4CAF50))
                                        .clip(RoundedCornerShape(3.dp))
                                )
                            }
                        }
                    }
                    
                    // 薄弱环节
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "薄弱环节",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .padding(bottom = 8.dp)
                                    .background(Color(0xFFE3F2FD))
                                    .clip(RoundedCornerShape(16.dp))
                                    .padding(8.dp, 4.dp)
                            ) {
                                Text(
                                    text = "冥想姿势调整",
                                    fontSize = 14.sp,
                                    color = Color(0xFF1976D2)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .padding(bottom = 8.dp)
                                    .background(Color(0xFFE3F2FD))
                                    .clip(RoundedCornerShape(16.dp))
                                    .padding(8.dp, 4.dp)
                            ) {
                                Text(
                                    text = "呼吸节奏控制",
                                    fontSize = 14.sp,
                                    color = Color(0xFF1976D2)
                                )
                            }
                        }
                    }
                }
            }
            
            // 状态评估部分
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(top = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "状态评估",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // 心理状态描述
                    Text(
                        text = "基于您的答题情况和学习表现，我们对您的当前心理状态评估如下：",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9))
                            .clip(RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "您的职场压力感知较咨询初期有所降低，情绪调节能力有明显提升。正念冥想练习对您的专注力和情绪稳定性产生了积极影响。",
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    
                    // 情绪状态对比
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                        Text(
                            text = "情绪状态变化",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            // 压力水平
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "压力水平", fontSize = 14.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "咨询前: 8", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "现在: 4", fontSize = 14.sp, color = Color(0xFF4CAF50))
                                }
                            }
                            
                            // 情绪稳定性
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "情绪稳定性", fontSize = 14.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "咨询前: 5", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "现在: 8", fontSize = 14.sp, color = Color(0xFF4CAF50))
                                }
                            }
                        }
                    }
                }
            }
            
            // 建议部分
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(top = 8.dp)
                    .padding(bottom = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "建议",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // 推荐行动
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFF5A67D8))
                                .clip(RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "1", color = Color.White, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "继续深化学习",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "建议继续练习正念冥想，每天保持15-20分钟的练习时间，巩固已学知识。",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 32.dp, top = 4.dp, bottom = 12.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFF5A67D8))
                                .clip(RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "2", color = Color.White, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "针对性练习薄弱环节",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "重点关注冥想姿势调整和呼吸节奏控制的练习，可以尝试使用辅助工具帮助规范动作。",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 32.dp, top = 4.dp, bottom = 12.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFF5A67D8))
                                .clip(RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "3", color = Color.White, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "推荐学习包",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "建议学习《深度放松技巧》学习包，帮助您更好地掌握身体放松方法。",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 32.dp, top = 4.dp, bottom = 12.dp)
                    )
                }
            }
        }
    }
    
    // 保存确认对话框
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text(text = "保存报告") },
            text = { Text(text = "报告已成功保存到您的设备中。") },
            confirmButton = {
                Button(onClick = { showSaveDialog = false }) {
                    Text(text = "确定")
                }
            }
        )
    }
    
    // 分享对话框
    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text(text = "分享报告") },
            text = { Text(text = "选择分享方式：") },
            confirmButton = {
                Button(onClick = { showShareDialog = false }) {
                    Text(text = "以图片形式分享")
                }
            },
            dismissButton = {
                Button(onClick = { showShareDialog = false }) {
                    Text(text = "取消")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AssessmentReportScreenPreview() {
    MentalTheme {
        AssessmentReportScreen()
    }
}