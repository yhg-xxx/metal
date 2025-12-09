package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import kotlinx.coroutines.*
import com.example.R
import com.example.model.Counselor
import com.example.model.SearchCounselorsRequest
import com.example.network.RetrofitClient
import com.example.ui.features.ChatDetailActivity
import com.example.ui.features.CounselorDetailActivity
import com.example.ui.features.QuickConsultationActivity
import com.example.ui.theme.MentalTheme
import com.example.util.CounselorUtils
import com.example.util.ImageLoadingUtils
import timber.log.Timber



/**
 * 首页屏幕组件
 * 实现图片样式的主页，包含搜索栏、主要功能入口、推荐咨询师等内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier, onNavigateToSearch: () -> Unit, isFromAdScreen: Boolean = false) {
    val context = LocalContext.current
    
    // 导航到快速咨询页面
    val navigateToQuickConsultation: () -> Unit = {
        val intent = Intent(context, QuickConsultationActivity::class.java)
        context.startActivity(intent)
    }
    var counselors by remember { mutableStateOf<List<Counselor>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // 升级服务弹窗状态
    var showUpgradePopup by remember { mutableStateOf(false) }

    
    // 当isFromAdScreen参数为true时检查是否需要显示对话框
    LaunchedEffect(isFromAdScreen) {
        // 添加500ms延迟，确保页面加载完成后再显示弹窗
        delay(500)

        // 简化条件：只要是从广告页跳转过来就显示弹窗
        if (isFromAdScreen) {
            showUpgradePopup = true
        }
    }
    
    // 点击升级服务按钮显示弹窗
    val showUpgradePopupHandler: () -> Unit = {
        showUpgradePopup = true
    }

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
        // 显示升级服务弹窗
        if (showUpgradePopup) {
            UpgradeServicePopup(
                onDismiss = { showUpgradePopup = false }
            )
        }
        
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
            
            // 升级服务按钮
            item {
                UpgradeServiceBanner(onClick = showUpgradePopupHandler)
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary // 设置为纯白色
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                            Text(
                                text = "加载中...", 
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                } else if (error != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary // 设置为纯白色
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                            Text(
                                text = error!!, 
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                } else if (counselors != null && counselors!!.isNotEmpty()) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        counselors!!.forEachIndexed { index, counselor ->
                            CounselorItem(counselor = counselor)
                            if (index < counselors!!.size - 1) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary // 设置为纯白色
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
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
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clickable { onClick?.invoke() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary // 设置为纯白色
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize() // 确保Row占满Card高度
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧图标
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center // 确保图片在Box内居中
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
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.Center // 确保文字垂直居中
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
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "进入",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(16.dp)
                    .padding(16.dp)
            )
        }
    }
}

/**
 * 咨询师项组件
 */
@Composable
private fun CounselorItem(counselor: Counselor) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary // 设置为纯白色
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
            // 移除不必要的debug日志，减少日志输出
            // Timber.d("加载咨询师头像: name=${counselor.realName}, originalUrl=${counselor.photoUrl}, processedUrl=$imageUrl")
            
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
                        // 移除成功日志，减少日志输出
                        // Timber.d("头像加载成功: ${counselor.realName}")
                    }
                )
            }
            
            // 咨询师信息
            Column(
                modifier = Modifier.weight(1f).padding(start = 12.dp)
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
                    .clip(RoundedCornerShape(8.dp)) // 增加圆角效果
                    .background(MaterialTheme.colorScheme.primaryContainer) // 使用新的按钮蓝色
                    .clickable {
                        // 处理私聊按钮点击事件
                        val dbHelper = com.example.util.DatabaseHelper(context)
                        val loggedInUser = dbHelper.getLoggedInUser()
                        val userId = loggedInUser?.id?.toLong() ?: 0L
                        
                        if (userId > 0 && counselor.counselorId > 0) {
                            // 启动协程调用API
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    // 首先查询用户是否与目标咨询师存在历史对话
                                    val response = withContext(Dispatchers.IO) {
                                        RetrofitClient.apiService.getUserConversatedCounselors(userId)
                                    }
                                    val hasExistingConversation = response.data?.any { 
                                        it.counselorId.toLong() == counselor.counselorId.toLong() 
                                    } == true
                                    
                                    // 根据是否存在历史对话执行不同逻辑
                                    if (!hasExistingConversation) {
                                        // 不存在历史对话，创建初始对话
                                        withContext(Dispatchers.IO) {
                                            RetrofitClient.apiService.createInitialConversation(
                                                userId = userId,
                                                counselorId = counselor.counselorId.toLong()
                                            )
                                        }
                                    }
                                    
                                    // 无论是否创建新对话，都跳转到聊天详情页
                                    ChatDetailActivity.start(context, userId, counselor)
                                } catch (e: Exception) {
                                    // 显示错误提示
                                    Toast.makeText(
                                        context,
                                        "操作失败: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "请先登录",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
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
}

/**
 * 升级服务横幅组件
 */
@Composable
private fun UpgradeServiceBanner(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(80.dp)
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 左侧文字内容
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "首单半价",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                .background(Color.White.copy(alpha = 0.2f))
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            Text(
                                text = "点击咨询",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                    Text(
                        text = "轻松开启心灵对话",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                
                // 右侧图片
                Image(
                    painter = painterResource(id = R.drawable.mental2), // 使用现有图片资源
                    contentDescription = "升级服务",
                    modifier = Modifier.size(60.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

/**
 * 升级服务对话框组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpgradeServicePopup(
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300)) + scaleOut(animationSpec = tween(300))
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "首单半价",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "欢迎使用央心心理服务！\n\n首次咨询可享受半价优惠，专业心理咨询师为您提供一对一服务，帮助您解决心理困扰，开启健康生活。\n\n立即预约，享受专属优惠！",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "关闭",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp)
        )
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