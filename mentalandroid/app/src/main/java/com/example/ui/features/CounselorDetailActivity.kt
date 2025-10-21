package com.example.ui.features

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Counselor
import com.example.network.RetrofitClient
import com.example.ui.theme.MentalTheme
import com.example.util.CounselorUtils
import com.example.util.IpAddressManager
import androidx.core.view.WindowCompat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class CounselorDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 启用边缘到边缘显示
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 设置沉浸式状态栏
        setupImmersiveStatusBar()

        val counselorId = intent.getIntExtra("counselorId", -1)

        setContentView(
            ComposeView(this).apply {
                setContent {
                    MentalTheme {
                        CounselorDetailScreen(
                            counselorId = counselorId,
                            onBackPress = { finish() }
                        )
                    }
                }
            }
        )
    }

    private fun setupImmersiveStatusBar() {
        // 让布局可以全屏，延展到状态栏里
        WindowCompat.getInsetsController(window, window.decorView).let { controller ->
            controller.isAppearanceLightStatusBars = false // 设置状态栏图标为浅色（白色）
        }

        // 设置状态栏颜色为透明
        window.statusBarColor = android.graphics.Color.TRANSPARENT
    }
}

/**
 * 咨询师详情页面
 * 提供咨询师详情以及预约功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounselorDetailScreen(counselorId: Int, onBackPress: () -> Unit) {
    var counselor by remember { mutableStateOf<Counselor?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // 获取咨询师详情
    LaunchedEffect(counselorId) {
        isLoading = true
        try {
            withContext(Dispatchers.IO) {
                counselor = RetrofitClient.apiService.getCounselorDetail(counselorId)
            }
        } catch (e: Exception) {
            error = "获取咨询师详情失败: ${e.message}"
            Timber.e(e, "Failed to fetch counselor detail")
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            // 使用与首页一致的TopAppBar实现
            TopAppBar(
                title = {
                    Text(
                        text = "咨询师详情",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    // 右侧占位元素，保持标题居中
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "加载中...", fontSize = 16.sp)
            }
        } else if (error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = error!!, color = Color.Red, fontSize = 16.sp)
            }
        } else if (counselor != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF7F7F7))
            ) {
                // 咨询师基本信息
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // 头像
                            val imageUrl = IpAddressManager.processImageUrl(counselor!!.photoUrl)
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "${counselor!!.realName}的头像",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                            )

                            // 基本信息
                            Column(modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = counselor!!.realName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${counselor!!.yearsOfExperience}年从业经验",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "评分",
                                        tint = Color.Yellow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${counselor!!.rating}",
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    Text(
                                        text = "· 已服务${counselor!!.totalSessions}人次",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                // 咨询费用
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "咨询费用",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(80.dp)
                            )
                            Text(
                                text = "¥${counselor!!.consultationFee}/次起",
                                fontSize = 16.sp,
                                color = Color.Red
                            )
                        }
                    }
                }

                // 擅长领域
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "擅长领域",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Text(
                                text = CounselorUtils.parseSpecialization(counselor!!.specialization),
                                fontSize = 14.sp,
                                color = Color(0xFF333333)
                            )
                        }
                    }
                }

                // 治疗流派
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "治疗流派",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Text(
                                text = CounselorUtils.parseTherapeuticApproach(counselor!!.therapeuticApproach),
                                fontSize = 14.sp,
                                color = Color(0xFF333333)
                            )
                        }
                    }
                }

                // 个人介绍
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "个人介绍",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Text(
                                text = counselor!!.introduction ?: "暂无个人介绍",
                                fontSize = 14.sp,
                                color = Color(0xFF333333),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // 服务信息
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "服务信息",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // 服务类型
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "服务类型:",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.width(80.dp)
                                )
                                Text(
                                    text = CounselorUtils.parseServiceTypes(counselor!!.serviceTypes),
                                    fontSize = 14.sp,
                                    color = Color(0xFF333333)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // 可用日期
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "可用日期:",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.width(80.dp)
                                )
                                Text(
                                    text = CounselorUtils.parseAvailableDays(counselor!!.availableDays),
                                    fontSize = 14.sp,
                                    color = Color(0xFF333333)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // 工作时间
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "工作时间:",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.width(80.dp)
                                )
                                Text(
                                    text = CounselorUtils.parseWorkingHours(counselor!!.workingHours),
                                    fontSize = 14.sp,
                                    color = Color(0xFF333333)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // 咨询时长
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "咨询时长:",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.width(80.dp)
                                )
                                Text(
                                    text = "${CounselorUtils.parseSessionDurations(counselor!!.sessionDurations)}分钟",
                                    fontSize = 14.sp,
                                    color = Color(0xFF333333)
                                )
                            }
                        }
                    }
                }

                // 底部预约按钮
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary)
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .clickable {},
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "立即预约",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}