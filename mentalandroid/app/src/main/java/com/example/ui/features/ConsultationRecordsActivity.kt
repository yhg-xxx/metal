package com.example.ui.features

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.ComposeView
import com.example.ui.theme.LightGrayBackground
import com.example.ui.theme.MentalTheme
import com.example.model.QuickConsultation
import com.example.network.RetrofitClient
import com.example.util.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConsultationRecordsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ComposeView(this).apply {
            setContent {
                MentalTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        ConsultationRecordsScreen(context = this@ConsultationRecordsActivity)
                    }
                }
            }
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultationRecordsScreen(context: AppCompatActivity) {
    var consultationRecords by remember { mutableStateOf<List<QuickConsultation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            // 获取当前登录用户的ID
            val databaseHelper = DatabaseHelper(context)
            val loggedInUser = databaseHelper.getLoggedInUser()
            
            if (loggedInUser != null) {
                val userId = loggedInUser.id
                Timber.tag("ConsultationRecords").d("当前登录用户ID: $userId")
                
                // 调用API获取咨询记录
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getUserConsultationRecords(userId)
                }
                
                if (response.code == 200) {
                    consultationRecords = response.data ?: emptyList()
                    Timber.tag("ConsultationRecords")
                        .d("获取到咨询记录数量: ${consultationRecords.size}")
                } else {
                    error = "获取咨询记录失败: ${response.message}"
                    Timber.tag("ConsultationRecords").e(error ?: "未知错误")
                }
            } else {
                error = "未找到登录用户"
                Timber.tag("ConsultationRecords").e("未找到登录用户")
            }
        } catch (e: Exception) {
            error = "获取咨询记录时发生错误: ${e.message}"
            Timber.tag("ConsultationRecords").e("异常: ${e.message}")
        } finally {
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "咨询记录",
                        color = MaterialTheme.colorScheme.onPrimary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { context.finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
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
    ) {
        paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .background(LightGrayBackground)
            .padding(paddingValues), 
            horizontalAlignment = Alignment.CenterHorizontally) {
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "加载中...")
                }
            } else if (error != null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = error ?: "未知错误", color = MaterialTheme.colorScheme.error)
                }
            } else if (consultationRecords.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "暂无咨询记录")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(consultationRecords) {record ->
                        ConsultationRecordCard(record = record)
                    }
                }
            }
        }
    }
}

@Composable
fun ConsultationRecordCard(record: QuickConsultation) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = record.problemDescription,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (record.status) {
                        "PENDING" -> "待匹配"
                        "MATCHED" -> "已匹配"
                        else -> record.status
                    },
                    color = if (record.status == "MATCHED") {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row {
                Text(text = "问题持续时间: ")
                Text(text = record.problemDuration)
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row {
                Text(text = "咨询方式: ")
                Text(text = when (record.preferredMethod) {
                    "TEXT" -> "文字咨询"
                    "VIDEO" -> "视频咨询"
                    else -> record.preferredMethod
                })
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            if (record.matchedCounselorId != null) {
                Row {
                    Text(text = "匹配咨询师ID: ")
                    Text(text = record.matchedCounselorId.toString())
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "提交时间: ${formatDateTime(record.createdTime)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun formatDateTime(dateTimeString: String?): String {
    if (dateTimeString.isNullOrEmpty()) return ""
    
    try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val date: Date? = inputFormat.parse(dateTimeString)
        return date?.let { outputFormat.format(it) } ?: dateTimeString
    } catch (e: Exception) {
        Timber.tag("ConsultationRecords").e("日期格式化错误: ${e.message}")
        return dateTimeString
    }
}