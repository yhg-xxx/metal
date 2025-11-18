package com.example.ui.screens

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import android.content.Context
import android.content.Intent
import com.example.ui.features.VideoDetailActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LearningPackage
import com.example.network.RetrofitClient
import com.example.ui.theme.MentalTheme
import com.example.util.ImageLoadingUtils
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 学习屏幕组件
 * 包含个性化学习包推荐、视频学习等功能
 */
// 跳转到视频详情页的辅助函数
fun navigateToVideoDetail(context: Context, learningPackage: LearningPackage) {
    val intent = Intent(context, VideoDetailActivity::class.java)
    intent.putExtra("learningPackageId", learningPackage.id.toLong())
    intent.putExtra("learningPackageTitle", learningPackage.title)
    context.startActivity(intent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(modifier: Modifier = Modifier) {
    val apiService = RetrofitClient.apiService
    val context = LocalContext.current
    
    // 保存context以供非Composable函数使用
    val savedContext = remember { context }
    
    var learningPackages by remember { mutableStateOf<List<LearningPackage>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }



    // 加载学习包数据
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val response = withContext(Dispatchers.IO) {
                apiService.getLearningPackages()
            }
            
            if (response.code == 200) {
                // 显示所有获取到的学习包数据
                learningPackages = response.data
            } else {
                errorMessage = "获取学习包失败: ${response.message}"
                Timber.tag("LearningScreen").e(errorMessage.toString())
            }
        } catch (e: Exception) {
            errorMessage = "网络请求异常: ${e.message}"
            Timber.tag("LearningScreen").e(e, "Failed to fetch learning packages")
        } finally {
            isLoading = false
        }
    }

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
            RecommendedLearningPackage(learningPackages)
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
                if (isLoading) {
                    // 显示加载状态
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "加载中...", color = Color.Gray)
                    }
                } else if (errorMessage != null) {
                    // 显示错误信息
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = errorMessage ?: "加载失败", color = Color.Red)
                    }
                } else if (learningPackages?.isNotEmpty() == true) {
                    // 使用从API获取的数据
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        learningPackages?.forEachIndexed { index, packageItem ->
                            LearningPackageItem(packageItem, onPackageClick = { navigateToVideoDetail(savedContext, it) })
                            if (index < (learningPackages?.size ?: 0) - 1) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                } else {
                    // 显示空状态
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "暂无学习包", color = Color.Gray)
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
 * 使用学习包数据的第一条作为推荐内容
 */
@Composable
private fun RecommendedLearningPackage(learningPackages: List<LearningPackage>?) {
    // 获取学习包数据的第一条（如果有）
    val recommendedPackage = learningPackages?.firstOrNull()
    val context = LocalContext.current
    
    if (recommendedPackage != null) {
        // 使用学习包的description作为推荐原因
        val recommendedReason = recommendedPackage.description
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .clickable { navigateToVideoDetail(context, recommendedPackage) }
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
                    // 使用ImageLoadingUtils处理图片URL并加载
                    AsyncImage(
                        model = ImageLoadingUtils.processImageUrl(recommendedPackage.coverImageUrl),
                        contentDescription = recommendedPackage.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = recommendedPackage.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${recommendedPackage.videoCount}个视频 · 预计${recommendedPackage.estimatedDurationMinutes}分钟",
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
    } else {
        // 如果没有学习包数据，显示空状态
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(120.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = "暂无推荐内容",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Center)
            )
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
            .clip(RoundedCornerShape(18.dp)) // 统一的分类按钮圆角样式
            .background(if (text == "全部") MaterialTheme.colorScheme.primaryContainer else Color.White)
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
private fun LearningPackageItem(learningPackage: LearningPackage, onPackageClick: (LearningPackage) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable { onPackageClick(learningPackage) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 学习包封面
        AsyncImage(
            model = ImageLoadingUtils.processImageUrl(learningPackage.coverImageUrl),
            contentDescription = "${learningPackage.title} 学习包",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(80.dp)
                .height(80.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        
        // 学习包信息 - 显示真实的视频数量和预计学习时间
        Column(
            modifier = Modifier.weight(1f).padding(12.dp)
        ) {
            Text(
                text = learningPackage.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${learningPackage.videoCount} 个视频 · 预计${learningPackage.estimatedDurationMinutes}分钟",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        
        // 学习按钮
        IconButton(
            onClick = { onPackageClick(learningPackage) },
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp)) // 统一的学习按钮圆角样式
                .background(MaterialTheme.colorScheme.primaryContainer)
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
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable {}
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier
                    .size(80.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
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
                                .clip(RoundedCornerShape(1.dp))
                                .background(Color(0xFFEEEEEE))
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(2.dp)
                                    .width((progress * 100).dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(Color(0xFF5A67D8))
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