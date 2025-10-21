package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.compose.foundation.background
import com.example.ui.features.CounselorDetailActivity
import com.example.ui.features.QuickConsultationActivity

import com.example.R
import com.example.model.Counselor
import com.example.util.CounselorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import com.example.model.SearchCounselorsRequest
import com.example.network.RetrofitClient
import com.example.ui.theme.MentalTheme
import com.example.util.ImageLoadingUtils


/**
 * 首页屏幕组件
 * 实现图片样式的主页，包含搜索栏、主要功能入口、推荐咨询师等内容
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    
    Scaffold(
        topBar = {
            // 使用Material3的TopAppBar组件
            TopAppBar(
                title = {
                    Text(
                        text = "央 心 心 理",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    // 搜索框和搜索按钮整合到TopAppBar中
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .height(40.dp)
                            .width(240.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { onNavigateToSearch() }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "搜索",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "亲子教育",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                },
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
            // 搜索功能已整合到TopAppBar中
            item {
                Spacer(modifier = Modifier.height(8.dp))
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
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Text(
                        text = "查看全部 >",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable { onNavigateToSearch() }
                    )
                }
                
                // 咨询师列表
                Spacer(modifier = Modifier.height(12.dp))
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            text = "加载中...", 
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                } else if (error != null) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            text = error!!, 
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
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
                        Text(
                            text = "暂无咨询师数据", 
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
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
        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            .background(MaterialTheme.colorScheme.surface)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick?.invoke() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧图标
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(60.dp)
                .background(MaterialTheme.colorScheme.primaryContainer)
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
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subText,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // 右侧箭头图标
        Icon(
            imageVector = Icons.Filled.ArrowForward,
            contentDescription = "进入",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(16.dp)
                .padding(16.dp)
        )
    }
}

// 测试图片加载功能已抽取到ImageLoadingUtils中



/**
 * 咨询师项组件
 */
@Composable
private fun CounselorItem(counselor: Counselor) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
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
        val imageUrl = ImageLoadingUtils.processImageUrl(counselor.photoUrl)
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
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = CounselorUtils.getQualificationLabel(counselor),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${counselor.realName} 从业${counselor.yearsOfExperience}年 · 咨询人数${counselor.totalSessions}人",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text(
                    text = CounselorUtils.getServiceLabels(counselor),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "¥${counselor.consultationFee}起",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        
        // 私聊按钮
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primary)
                .clip(RoundedCornerShape(5.dp))
                .clickable {},
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "私聊",
                color = MaterialTheme.colorScheme.onPrimary,
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