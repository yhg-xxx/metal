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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.MentalTheme

/**
 * 视频播放器屏幕组件
 * 包含视频播放控制、倍速调节、进度管理和学习验证功能
 */
@Composable
fun VideoPlayerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
    // 视频状态
    var isPlaying by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableFloatStateOf(0.3f) } // 模拟当前进度
    var currentTime by remember { mutableStateOf("2:30") }
    var totalTime by remember { mutableStateOf("7:45") }
    
    // 倍速状态
    val playbackSpeeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
    var selectedSpeed by remember { mutableStateOf(1.0f) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    
    // 学习验证状态
    var showVerificationDialog by remember { mutableStateOf(false) }
    var verificationAnswers by remember { mutableStateOf(listOf("深呼吸练习", "渐进式肌肉放松", "冥想", "运动")) }
    var selectedAnswer by remember { mutableStateOf(verificationAnswers[0]) }
    
    // 模拟视频播放完成
    val handleVideoComplete = { showVerificationDialog = true }
    
    Column(modifier = modifier.fillMaxSize().background(Color.Black)) {
        // 顶部导航栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF121212))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* 返回按钮点击事件 */ }) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "正念冥想入门：5分钟练习",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { /* 分享按钮点击事件 */ }) {
                Icon(imageVector = Icons.Filled.Share, contentDescription = "分享", tint = Color.White)
            }
        }
        
        // 视频播放区域（模拟）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color(0xFF1E1E1E))
                .clickable { isPlaying = !isPlaying },
            contentAlignment = Alignment.Center
        ) {
            // 模拟视频内容
            Text(
                text = "视频播放区域",
                color = Color.White,
                fontSize = 18.sp
            )
            
            // 播放/暂停按钮（仅在未播放时显示）
            if (!isPlaying) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0x80000000))
                        .clip(RoundedCornerShape(30.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "播放",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
        
        // 视频信息和控制区域
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E)).padding(16.dp)) {
            // 视频标题
            Text(
                text = "正念冥想入门：5分钟练习",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // 视频描述
            Text(
                text = "本视频将指导您进行简单的5分钟正念冥想练习，帮助您放松身心、缓解压力。",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // 进度条
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = currentTime, color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .height(3.dp)
                            .weight(1f)
                            .background(Color.Gray)
                            .clip(RoundedCornerShape(1.5.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .height(3.dp)
                                .width((currentProgress * 100).dp)
                                .background(Color(0xFF5A67D8))
                                .clip(RoundedCornerShape(1.5.dp))
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = totalTime, color = Color.White, fontSize = 12.sp)
                }
            }
            
            // 控制按钮
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* 重播按钮点击事件 */ }) {
                    Icon(imageVector = Icons.Filled.Replay, contentDescription = "重播", tint = Color.White)
                }
                
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFF5A67D8))
                        .clip(RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { isPlaying = !isPlaying }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "暂停" else "播放",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                Box(modifier = Modifier.clickable { showSpeedDialog = true }) {
                    Text(
                        text = "${selectedSpeed}x",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // 底部按钮
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { /* 下载按钮点击事件 */ },
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Text(text = "下载视频")
                }
                
                Button(
                    onClick = { handleVideoComplete() },
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Text(text = "标记为已完成")
                }
            }
        }
    }
    
    // 倍速选择对话框
    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text(text = "选择播放速度") },
            text = {
                Column {
                    playbackSpeeds.forEach { speed ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { 
                                selectedSpeed = speed
                                showSpeedDialog = false
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedSpeed == speed,
                                onClick = { 
                                    selectedSpeed = speed
                                    showSpeedDialog = false
                                }
                            )
                            Text(text = "${speed}x")
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showSpeedDialog = false }) {
                    Text(text = "确定")
                }
            },
            properties = DialogProperties()
        )
    }
    
    // 学习验证对话框
    if (showVerificationDialog) {
        AlertDialog(
            onDismissRequest = { showVerificationDialog = false },
            title = { Text(text = "学习验证") },
            text = {
                Column {
                    Text(
                        text = "本视频提到的缓解压力的方法是什么？",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    verificationAnswers.forEach { answer ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { 
                                selectedAnswer = answer
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedAnswer == answer,
                                onClick = { 
                                    selectedAnswer = answer
                                }
                            )
                            Text(text = answer)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { 
                    showVerificationDialog = false
                    // 这里可以添加验证逻辑和完成标记
                }) {
                    Text(text = "提交")
                }
            },
            properties = DialogProperties()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VideoPlayerScreenPreview() {
    MentalTheme {
        VideoPlayerScreen()
    }
}