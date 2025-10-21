package com.example.ui.screens

import kotlin.random.Random

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.MentalTheme

/**
 * 学习屏幕组件
 * 包含个性化学习包推荐、视频学习等功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "学习",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { /* 更多选项按钮点击事件 */ }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "更多选项",
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
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = paddingValues.calculateTopPadding(),
                bottom = 80.dp
            )
        ) {
            // 推荐学习包
            item {
                Text(
                    text = "为您推荐",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                
                // 个性化推荐学习包
                RecommendedLearningPackage()
            }
            
            // 学习包分类
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "学习分类",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                    LazyRow(modifier = Modifier.padding(horizontal = 16.dp)) {
                        val categories = listOf("全部", "情绪调节", "人际沟通", "职场心理", "亲子教育", "压力管理")
                        items(categories.size) {
                            LearningCategoryItem(categories[it])
                        }
                    }
                }
            
            // 热门学习包
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "热门学习包",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Text(
                        text = "查看全部 >",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                
                // 热门学习包列表
                Spacer(modifier = Modifier.height(12.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    for (i in 0 until 3) {
                        LearningPackageItem()
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
            
            // 最近学习
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "最近学习",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                
                // 最近学习视频列表
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    for (i in 0 until 2) {
                        RecentLearningVideoItem()
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

/**
 * 推荐学习包组件
 */
@Composable
private fun RecommendedLearningPackage() {
    val recommendedReason = "针对您的职场压力困扰，推荐以下缓解技巧视频"
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(Color.White)
            .clip(RoundedCornerShape(10.dp))
            .clickable {}
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "推荐",
                    tint = Color(0xFFF5A623),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "个性化推荐",
                    fontSize = 14.sp,
                    color = Color(0xFFF5A623),
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = recommendedReason,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img),
                    contentDescription = "职场压力管理学习包",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "职场压力管理学习包",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "5个视频 · 专业心理咨询师讲解",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "更多",
                    tint = Color.Gray
                )
            }
        }
    }
}

/**
 * 学习分类项组件
 */
@Composable
private fun LearningCategoryItem(text: String) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .background(if (text == "全部") Color(0xFF5A67D8) else Color.White)
            .clip(RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp)
            .clickable {},
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = if (text == "全部") Color.White else Color.Gray
        )
    }
    Spacer(modifier = Modifier.width(8.dp))
}

/**
 * 学习包项组件
 */
@Composable
private fun LearningPackageItem() {
    val packageNames = listOf("情绪管理基础", "有效沟通技巧", "亲子关系建立", "压力缓解策略")
    val randomName = packageNames.random()
    val videoCount = (3..5).random()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clip(RoundedCornerShape(8.dp))
            .clickable {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 学习包封面
        Image(
            painter = painterResource(id = R.drawable.img),
            contentDescription = "$randomName 学习包",
            modifier = Modifier
                .width(80.dp)
                .height(80.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        
        // 学习包信息
        Column(
            modifier = Modifier.weight(1f).padding(12.dp)
        ) {
            Text(
                text = randomName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$videoCount 个视频课程 · ${(1000..9999).random()} 人已学习",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "评分",
                        tint = Color(0xFFF5A623),
                        modifier = Modifier.size(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${(4.5 + Random.nextDouble() * 0.5).toString().take(3)}分",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
        
        // 学习按钮
        IconButton(
            onClick = { /* 学习按钮点击事件 */ },
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF5A67D8))
                .clip(RoundedCornerShape(20.dp))
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "开始学习",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 最近学习视频项组件
 */
@Composable
private fun RecentLearningVideoItem() {
    val videoNames = listOf(
        "正念冥想入门：5分钟练习",
        "有效沟通的3个关键技巧",
        "如何应对工作压力",
        "建立健康的亲子沟通"
    )
    val randomName = videoNames.random()
    val totalDuration = (5..15).random()
    val watchedDuration = (1..totalDuration-1).random()
    val progress = watchedDuration.toFloat() / totalDuration
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clip(RoundedCornerShape(8.dp))
            .clickable {}
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier) {
                    Image(
                        painter = painterResource(id = R.drawable.img),
                        contentDescription = randomName,
                        modifier = Modifier
                            .size(80.dp)
                            .height(60.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0x80000000))
                            .clip(CircleShape)
                            .align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "播放",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp).align(Alignment.Center)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = randomName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .weight(1f)
                                .background(Color(0xFFEEEEEE))
                                .clip(RoundedCornerShape(1.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(2.dp)
                                    .width((progress * 100).dp)
                                    .background(Color(0xFF5A67D8))
                                    .clip(RoundedCornerShape(1.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$watchedDuration/$totalDuration 分钟",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun LearningScreenPreview() {
    MentalTheme {
        LearningScreen()
    }
}