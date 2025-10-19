package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
 * 动态屏幕组件
 * 包含动态内容流、话题分类和用户发布的动态内容
 */
@Composable
fun DynamicScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF7F7F7))) {
        // 顶部标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF5A67D8))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "动态",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Row {
                IconButton(onClick = { /* 发布动态按钮点击事件 */ }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "发布动态",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { /* 更多选项按钮点击事件 */ }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "更多选项",
                        tint = Color.White
                    )
                }
            }
        }
        
        // 话题分类
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
        ) {
            val topics = listOf("推荐", "心理咨询", "冥想正念", "情感关系", "职场心理", "家庭教育", "心理健康")
            items(topics.size) {
                TopicItem(topics[it])
            }
        }
        
        // 动态内容流
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
        ) {
            // 模拟动态数据
            items(10) {
                DynamicPostItem()
            }
        }
    }
}

/**
 * 话题项组件
 */
@Composable
private fun TopicItem(text: String) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .background(if (text == "推荐") Color(0xFF5A67D8) else Color(0xFFF0F0F0))
            .clip(RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp)
            .clickable {},
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = if (text == "推荐") Color.White else Color.Gray
        )
    }
    Spacer(modifier = Modifier.width(8.dp))
}

/**
 * 动态帖子项组件
 */
@Composable
private fun DynamicPostItem() {
    Column(modifier = Modifier.background(Color.White)) {
        // 用户信息
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.img),
                contentDescription = "用户头像",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "心理咨询师${(0..999).random()}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${(1..30).random()}分钟前",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { /* 更多选项按钮点击事件 */ }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "更多选项",
                    tint = Color.Gray
                )
            }
        }
        
        // 帖子内容
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(
                text = "${listOf("今天分享一个", "最近在学习", "给大家推荐一个", "关于")[listOf(0, 1, 2, 3).random()]} ${listOf("冥想技巧", "心理调节方法", "情绪管理策略", "减压小妙招")[listOf(0, 1, 2, 3).random()]}，希望对大家有所帮助...",
                fontSize = 14.sp
            )
            
            // 帖子图片
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.img),
                    contentDescription = "帖子图片",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
        
        // 互动按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .border(BorderStroke(1.dp, Color(0xFFEEEEEE)))
        ) {
            InteractionButton("点赞", (10..999).random())
            Spacer(modifier = Modifier.weight(1f))
            InteractionButton("评论", (0..99).random())
            Spacer(modifier = Modifier.weight(1f))
            InteractionButton("收藏", (0..50).random())
            Spacer(modifier = Modifier.weight(1f))
            InteractionButton("分享", 0)
        }
        
        // 分割线
        Spacer(modifier = Modifier.height(8.dp).fillMaxWidth().background(Color(0xFFF7F7F7)))
    }
}

/**
 * 互动按钮组件
 */
@Composable
private fun InteractionButton(text: String, count: Int) {
    Row(
        modifier = Modifier.clickable {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.Gray
        )
        if (count > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = count.toString(),
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DynamicScreenPreview() {
    MentalTheme {
        DynamicScreen()
    }
}