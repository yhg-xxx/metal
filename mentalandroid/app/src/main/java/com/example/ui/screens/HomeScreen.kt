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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import coil.compose.AsyncImage
import androidx.compose.material3.Button
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import com.example.ui.features.CounselorDetailActivity
import com.example.ui.features.QuickConsultationActivity

import com.example.R
import com.example.model.Counselor
import com.example.util.CounselorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import com.example.model.SearchCounselorsRequest
import com.example.network.RetrofitClient
import com.example.ui.theme.MentalTheme
import com.example.util.IpAddressManager



/**
 * 首页屏幕组件
 * 实现图片样式的主页，包含搜索栏、主要功能入口、推荐咨询师等内容
 */
@Composable
fun HomeScreen(modifier: Modifier = Modifier, onNavigateToSearch: () -> Unit) {
    val context = LocalContext.current
    
    // 导航到快速咨询页面
    val navigateToQuickConsultation: () -> Unit = {
        val intent = Intent(context, QuickConsultationActivity::class.java)
        context.startActivity(intent)
    }
    var counselors by remember { mutableStateOf<List<Counselor>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // 获取咨询师列表
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            withContext(Dispatchers.IO) {
                // 发送空请求体以获取所有咨询师
                val request = SearchCounselorsRequest()
                counselors = RetrofitClient.apiService.searchCounselors(request)
            }
        } catch (e: Exception) {
            error = "获取咨询师列表失败: \${e.message}"
            Timber.e(e, "Failed to fetch counselors")
        } finally {
            isLoading = false
        }
    }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7)),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
    ) {
        // 顶部搜索栏
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF5A67D8))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // 应用标题
                Text(
                    text = "央心心理",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // 搜索框
            Row(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(horizontal = 16.dp)
                        .clickable { onNavigateToSearch() },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "搜索",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "亲子教育",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { onNavigateToSearch() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF5A67D8))

                ) {
                    Text(
                        text = "搜索",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // 平台保障
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PlatformGuaranteeItem("平台护航")
                PlatformGuaranteeItem("资质担保")
                PlatformGuaranteeItem("不满意退款")
                PlatformGuaranteeItem("隐私保障")
            }
        }
        
        // 主要功能入口
        item {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureEntryItem("心理测评", R.drawable.mental)
                FeatureEntryItem("快速咨询", R.drawable.mental2, onClick = navigateToQuickConsultation)
            }
        }

        // 推荐咨询师
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "推荐咨询师",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = "查看全部 >",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clickable { onNavigateToSearch() }
            )
            }
            
            // 咨询师列表
            Spacer(modifier = Modifier.height(12.dp))
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(text = "加载中...", modifier = Modifier.align(Alignment.Center))
                }
            } else if (error != null) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(text = error!!, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
            } else if (counselors != null && counselors!!.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    counselors!!.forEachIndexed { index, counselor ->
                        CounselorItem(counselor = counselor)
                        if (index < counselors!!.size - 1) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(text = "暂无咨询师数据", modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
    

}

/**
 * 平台保障项组件
 */
@Composable
private fun PlatformGuaranteeItem(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = Color.Gray,
        modifier = Modifier.clickable {}
    )
}

/**
 * 功能入口项组件
 */
@Composable
private fun FeatureEntryItem(text: String, imageRes: Int, onClick: (() -> Unit)? = null) {
    val subText = if (text == "心理测评") "测一测你是什么样的人" else "匹配合适的心理导师"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .background(Color.White)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick?.invoke() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧图标
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(60.dp)
                .background(Color(0xFFE6F0FF))
                .clip(RoundedCornerShape(12.dp))
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = text,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        // 中间文字内容
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subText,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        
        // 右侧箭头图标
        Icon(
            imageVector = Icons.Filled.ArrowForward,
            contentDescription = "进入",
            tint = Color.Gray,
            modifier = Modifier
                .size(16.dp)
                .padding(16.dp)
        )
    }
}

/**
 * 测试图片加载功能组件
 */
@Composable
private fun TestImageLoading() {
    val context = LocalContext.current
    val imageUrl = "http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg"
    val processedImageUrl = IpAddressManager.processImageUrl(imageUrl)
    var loadResult by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            Timber.d("开始测试图片加载: 原始URL=$imageUrl, 处理后URL=$processedImageUrl")
            
            coroutineScope.launch {
                try {
                    // 标记测试已启动
                    loadResult = "测试已启动，请查看日志..."
                    
                    // 这里不直接调用Composable函数，而是通过状态更新来触发UI变化
                    // 实际的图片加载将由下面的AsyncImage组件处理
                    
                    // 模拟网络请求延迟
                    withContext(Dispatchers.IO) {
                        delay(500)
                    }
                    
                } catch (e: Exception) {
                    loadResult = "图片加载异常: ${e.message ?: "未知异常"}"
                    Timber.e(e, "图片加载异常")
                }
            }
        }) {
            Text("测试图片加载")
        }
        
        if (loadResult != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = loadResult ?: "", fontSize = 14.sp, color = if (loadResult?.contains("成功") == true) Color.Green else Color.Red)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        AsyncImage(
            model = processedImageUrl,
            contentDescription = "测试图片",
            modifier = Modifier.size(200.dp),
            placeholder = painterResource(id = R.drawable.img),
            error = painterResource(id = R.drawable.img),
            contentScale = ContentScale.Crop,
            onSuccess = { 
                loadResult = "图片加载成功!"
                Timber.d("图片加载成功")
            },
            onError = { 
                loadResult = "图片加载失败"
                Timber.e("图片加载失败")
            }
        )
    }
}



/**
 * 咨询师项组件
 */
@Composable
private fun CounselorItem(counselor: Counselor) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clip(RoundedCornerShape(10.dp))
            .padding(12.dp)
            .clickable {
                val intent = Intent(context, CounselorDetailActivity::class.java)
                intent.putExtra("counselorId", counselor.counselorId)
                context.startActivity(intent)
            },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 咨询师头像
        // 处理和记录图片URL
        val imageUrl = IpAddressManager.processImageUrl(counselor.photoUrl)
        Timber.d("加载咨询师头像: name=${counselor.realName}, originalUrl=${counselor.photoUrl}, processedUrl=$imageUrl")
        
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            // 使用基本的AsyncImage配置，移除占位图
            AsyncImage(
                model = imageUrl,
                contentDescription = "${counselor.realName}的头像",
                modifier = Modifier.fillMaxSize(),
                // 仅保留错误图，移除占位图
                error = painterResource(id = R.drawable.img),
                contentScale = ContentScale.Crop,
                onError = { error ->
                    Timber.e("头像加载失败: $error, url=$imageUrl")
                },
                onSuccess = {
                    Timber.d("头像加载成功: ${counselor.realName}")
                }
            )
        }
        
        // 咨询师信息
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
        ) {
            Text(
                text = CounselorUtils.parseSpecialization(counselor.specialization),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = CounselorUtils.getQualificationLabel(counselor),
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${counselor.realName} 从业${counselor.yearsOfExperience}年 · 咨询人数${counselor.totalSessions}人",
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text(
                    text = CounselorUtils.getServiceLabels(counselor),
                    fontSize = 12.sp,
                    color = Color(0xFF5A67D8)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "¥${counselor.consultationFee}起",
                    fontSize = 12.sp,
                    color = Color.Red
                )
            }
        }
        
        // 私聊按钮
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF5A67D8))
                .clip(RoundedCornerShape(5.dp))
                .clickable {},
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "私聊",
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MentalTheme {
        HomeScreen(
            onNavigateToSearch = { /* 预览中不执行实际导航 */ }
        )
    }
}