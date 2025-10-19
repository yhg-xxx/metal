package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
 * 冥想屏幕组件
 * 包含冥想课程列表、分类和推荐内容的基本UI结构
 */
@Composable
fun MeditationScreen(modifier: Modifier = Modifier) {
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
                text = "冥想",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { /* 更多选项按钮点击事件 */ }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "更多选项",
                    tint = Color.White
                )
            }
        }
        
        // 主要内容区域
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
        ) {
            // 推荐冥想
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {}
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img),
                        contentDescription = "推荐冥想",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x80000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "深度放松冥想",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "15分钟 · 缓解压力",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            
            // 冥想分类
            item {
                Text(
                    text = "冥想分类",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                LazyRow(modifier = Modifier.padding(horizontal = 16.dp)) {
                    val categories = listOf("全部", "减压", "睡眠", "专注力", "情绪管理", "自我成长", "正念")
                    items(categories.size) {
                        MeditationCategoryItem(categories[it])
                    }
                }
            }
            
            // 推荐课程
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "推荐课程",
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
                
                // 课程列表
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(modifier = Modifier.padding(horizontal = 16.dp)) {
                    // 模拟课程数据
                    items(5) {
                        MeditationCourseItem()
                    }
                }
            }
            
            // 冥想专辑
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "冥想专辑",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                // 专辑列表
                Spacer(modifier = Modifier.height(12.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    // 模拟专辑数据
                    for (i in 0 until 3) {
                        MeditationAlbumItem()
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

/**
 * 冥想分类项组件
 */
@Composable
private fun MeditationCategoryItem(text: String) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .background(Color.White)
            .clip(RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp)
            .clickable {},
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = if (text == "全部") Color(0xFF5A67D8) else Color.Gray
        )
    }
    Spacer(modifier = Modifier.width(8.dp))
}

/**
 * 冥想课程项组件
 */
@Composable
private fun MeditationCourseItem() {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clickable {}
    ) {
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(100.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img),
                contentDescription = "冥想课程",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(Color(0x80000000))
                    .padding(4.dp, 2.dp)
            ) {
                Text(
                    text = "${(5..30).random()}分钟",
                    color = Color.White,
                    fontSize = 10.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${listOf("放松", "睡眠", "专注力", "减压")[listOf(0, 1, 2, 3).random()]}冥想",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${(1000..9999).random()}人在听",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
    Spacer(modifier = Modifier.width(12.dp))
}

/**
 * 冥想专辑项组件
 */
@Composable
private fun MeditationAlbumItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clip(RoundedCornerShape(8.dp))
            .clickable {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 专辑封面
        Image(
            painter = painterResource(id = R.drawable.img),
            contentDescription = "专辑封面",
            modifier = Modifier
                .width(80.dp)
                .height(80.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        
        // 专辑信息
        Column(
            modifier = Modifier.weight(1f).padding(12.dp)
        ) {
            Text(
                text = "${listOf("7天", "14天", "21天")[listOf(0, 1, 2).random()]}冥想之旅",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(3..20).random()}个冥想课程 · ${(1000..9999).random()}人已完成",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        
        // 播放按钮
        IconButton(
            onClick = { /* 播放按钮点击事件 */ },
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF5A67D8))
                .clip(RoundedCornerShape(20.dp))
                .padding(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img),
                contentDescription = "播放",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MeditationScreenPreview() {
    MentalTheme {
        MeditationScreen()
    }
}