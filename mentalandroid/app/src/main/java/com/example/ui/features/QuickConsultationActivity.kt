package com.example.ui.features

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle

import com.example.network.RetrofitClient
import com.example.ui.theme.MentalTheme
import com.example.util.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream


class QuickConsultationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MentalTheme {
                QuickConsultationScreen()
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun QuickConsultationScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // 表单状态
    var problemDescription by remember { mutableStateOf(TextFieldValue("")) }
    var problemDuration by remember { mutableStateOf("一个月") }
    var preferredMethod by remember { mutableStateOf("TEXT") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    
    // 问题持续时间选项
    val durationOptions = listOf("一个月", "三个月", "半年", "半年以上")
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // 加载状态和错误信息
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    
    // 图片选择启动器
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK && it.data != null) {
            selectedImageUri = it.data?.data
        }
    }
    
    // 提交表单
    fun submitForm() {
        // 表单验证
        if (problemDescription.text.isEmpty()) {
            errorMessage = "请输入问题描述"
            return
        }
        // 问题持续时间已经有默认值，不需要验证空值
        
        isLoading = true
        errorMessage = null
        
        coroutineScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                        // 准备请求参数
                        // 从本地数据库获取当前登录用户的id
                        val dbHelper = DatabaseHelper(context)
                        val loggedInUser = dbHelper.getLoggedInUser()
                        val userId = loggedInUser?.id?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                            ?: "".toRequestBody("text/plain".toMediaTypeOrNull())
                        val problemDescBody = problemDescription.text.toRequestBody("text/plain".toMediaTypeOrNull())
                        val problemDurBody = problemDuration.toRequestBody("text/plain".toMediaTypeOrNull())
                        val prefMethodBody = preferredMethod.toRequestBody("text/plain".toMediaTypeOrNull())
                        
                        // 处理图片上传
                        val files = selectedImageUri?.let {
                            // 在IO线程中定义并使用文件转换逻辑
                            val inputStream = context.contentResolver.openInputStream(it)
                            val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
                            
                            inputStream?.use { input ->
                                FileOutputStream(file).use { output ->
                                    val buffer = ByteArray(4 * 1024)
                                    var read: Int
                                    while (input.read(buffer).also { read = it } != -1) {
                                        output.write(buffer, 0, read)
                                    }
                                    output.flush()
                                }
                            }
                            
                            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                            val body = MultipartBody.Part.createFormData("files", file.name, requestFile)
                            listOf(body)
                        }
                        
                        // 调用API
                        RetrofitClient.apiService.submitQuickConsultation(
                            userId = userId,
                            problemDescription = problemDescBody,
                            problemDuration = problemDurBody,
                            preferredMethod = prefMethodBody,
                            files = files,
                            matchedCounselorId = null
                        )
                    }
                    
                    // 检查响应码
                    if (response.code == 200) {
                        // 提交成功
                        isSuccess = true
                    } else {
                        errorMessage = "提交失败: ${response.msg}"
                    }
            } catch (e: Exception) {
                errorMessage = "提交失败: ${e.message ?: "未知错误"}"
            } finally {
                isLoading = false
            }
        }
    }
    

    
    // 咨询方式选项
    val consultationMethods = listOf(
        Pair("TEXT", "文字咨询"),
        Pair("VOICE", "语音咨询"),
        Pair("VIDEO", "视频咨询")
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "快速咨询", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { (context as Activity).finish() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
        ) {
            if (isSuccess) {
                // 成功页面
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "咨询成功",
                        modifier = Modifier.size(100.dp),
                        tint = Color.Green
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "快速咨询申请已提交成功",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "我们会尽快为您匹配咨询师",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    Button(
                        onClick = { (context as Activity).finish() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "确定")
                    }
                }
            } else {
                // 表单页面
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 问题描述
                    item {
                        Text("核心心理问题描述", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = problemDescription,
                            onValueChange = { problemDescription = it },
                            placeholder = { Text("请详细描述您的心理问题") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4,
                            shape = RoundedCornerShape(8.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedIndicatorColor = Color(0xFF5A67D8),
                                unfocusedIndicatorColor = Color.LightGray
                            )
                        )
                    }
                    
                    // 问题持续时间
                    item {
                        Text("问题持续时间", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            ExposedDropdownMenuBox(
                                expanded = isDropdownExpanded,
                                onExpandedChange = { isDropdownExpanded = it }
                            ) {
                                TextField(
                                    readOnly = true,
                                    value = problemDuration,
                                    onValueChange = {},
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = isDropdownExpanded
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedIndicatorColor = Color(0xFF5A67D8),
                                        unfocusedIndicatorColor = Color.LightGray
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = isDropdownExpanded,
                                    onDismissRequest = { isDropdownExpanded = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    durationOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(text = option) },
                                            onClick = {
                                                problemDuration = option
                                                isDropdownExpanded = false
                                            },
                                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // 偏好咨询方式
                    item {
                        Text("偏好咨询方式", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            consultationMethods.forEach { (value, label) ->
                                val isSelected = preferredMethod == value
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(color = if (isSelected) Color(0xFF5A67D8) else Color.LightGray)
                                        .clickable { preferredMethod = value }
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color.White else Color.Black,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                    
                    // 图片上传
                    item {
                        Text("上传相关图片（选填）", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color = Color.LightGray)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                                    pickImageLauncher.launch(intent)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedImageUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = selectedImageUri),
                                    contentDescription = "已选图片",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AddCircle,
                                        contentDescription = "选择图片",
                                        modifier = Modifier.size(32.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "点击选择图片",
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                    
                    // 错误信息
                    if (errorMessage != null) {
                        item {
                            Text(
                                text = errorMessage ?: "",
                                color = Color.Red,
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    // 提交按钮
                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                        Button(
                            onClick = { submitForm() },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(text = "提交咨询申请", fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}