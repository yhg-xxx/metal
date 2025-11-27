package com.example.ui.features


import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Build
import androidx.core.view.WindowCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.User
import com.example.network.ApiResponse
import com.example.network.ApiService
import com.example.network.RetrofitClient
import com.example.ui.theme.MentalTheme
import com.example.util.DatabaseHelper
import com.example.util.IpAddressManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

// 格式化电话号码，中间部分显示为星号
fun formatPhoneNumber(phone: String): String {
    if (phone.length != 11) return phone
    return phone.substring(0, 3) + "****" + phone.substring(7)
}

// 从Uri创建临时文件
fun createTempFileFromUri(context: android.content.Context, uri: Uri): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "JPEG_${timeStamp}_"
    val file = File.createTempFile(fileName, ".jpg", context.cacheDir)
    
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }
    
    return file
}
@Suppress("DEPRECATION")
class ProfileEditActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private val apiService = RetrofitClient.apiService

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DatabaseHelper(this)

        // 启用边缘到边缘显示
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 设置沉浸式状态栏
        setupImmersiveStatusBar()

        setContent {
            MentalTheme {
                ProfileEditScreen(
                    dbHelper = dbHelper,
                    apiService = apiService,
                    onBack = { finish() }
                )
            }
        }
    }

    private fun setupImmersiveStatusBar() {
        // 让布局可以全屏，延展到状态栏里
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            false

        // 设置状态栏颜色为透明
        window.statusBarColor = android.graphics.Color.TRANSPARENT
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    dbHelper: DatabaseHelper,
    apiService: ApiService,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()


    // 获取当前登录用户信息
    var user by remember {
        mutableStateOf(dbHelper.getLoggedInUser() ?: User(
            username = "",
            phone = "",
            password = ""
        ))
    }
    
    // 表单字段状态
    var username by remember { mutableStateOf(user.username) }
    var nickname by remember { mutableStateOf(user.nickname ?: "") }
    var email by remember { mutableStateOf(user.email ?: "") }
    var gender by remember { mutableStateOf(user.gender) }
    var age by remember { mutableStateOf(user.age?.toString() ?: "") }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    
    // 加载状态
    var isSaving by remember { mutableStateOf(false) }
    
    // 年龄选择器状态
    var showAgeSelector by remember { mutableStateOf(false) }
    
    // 头像选择启动器
    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                avatarUri = it
            }
        }
    )
    
    // 保存用户信息
    fun saveUserInfo() {
        if (isSaving) return
        
        isSaving = true
        
        // 验证年龄
        val ageValue = age.toIntOrNull()
        if (age.isNotEmpty() && ageValue == null) {
            // 显示错误提示
            isSaving = false
            return
        }
        
        // 创建更新后的用户对象
        val updatedUser = user.copy(
            username = username,
            nickname = nickname.takeIf { it.isNotEmpty() },
            email = email.takeIf { it.isNotEmpty() },
            gender = gender,
            age = ageValue
        )
        
        coroutineScope.launch {
            try {
                // 1. 先保存到本地数据库
                dbHelper.addOrUpdateUser(updatedUser)
                
                // 2. 再调用API保存到远端服务器
                val responseUser: User = withContext(Dispatchers.IO) {
                    val localApiService = apiService
                    
                    // 准备用户信息JSON字符串
                    val userJson = """
                        {
                            "username": "${updatedUser.username}",
                            "password": "${updatedUser.password}",
                            "phone": "${updatedUser.phone}",
                            "email": ${if (updatedUser.email != null) "\"${updatedUser.email}\"" else "null"},
                            "nickname": ${if (updatedUser.nickname != null) "\"${updatedUser.nickname}\"" else "null"},
                            "gender": "${updatedUser.gender}",
                            "age": ${updatedUser.age ?: "null"}
                        }
                    """.trimIndent()
                    
                    val userMediaType = "application/json".toMediaTypeOrNull()
                    val userRequestBody = userMediaType?.let {
                        userJson.toRequestBody(it)
                    }
                    
                    // 准备头像文件
                    val avatarPart = avatarUri?.let { uri ->
                        val file = createTempFileFromUri(context, uri)
                        val fileMediaType = (context.contentResolver.getType(uri) ?: "image/jpeg").toMediaTypeOrNull()
                        val requestFile = fileMediaType?.let {
                            file.asRequestBody(it)
                        }
                        requestFile?.let {
                            MultipartBody.Part.createFormData(
                                "avatar",
                                file.name,
                                it
                            )
                        }
                    }
                    
                    // 判断是创建还是更新用户
                    val existingUser = dbHelper.getLoggedInUser()
                    val apiResponse = if (existingUser != null && existingUser.id > 0 && userRequestBody != null) {
                        // 更新用户
                        localApiService.updateUser(
                            phone = existingUser.phone,
                            user = userRequestBody,
                            avatar = avatarPart
                        )
                    } else if (userRequestBody != null) {
                        // 创建新用户
                        localApiService.createUser(
                            user = userRequestBody,
                            avatar = avatarPart
                        )
                    } else {
                        // 如果请求体为空，返回包含原用户信息的响应
                        ApiResponse(200, "success", updatedUser)
                    }
                    
                    // 从响应中获取用户数据
                    apiResponse.data ?: updatedUser
                }
                
                // 更新本地用户信息（包含从服务器返回的额外信息）
                // 处理avatarUrl前缀，使用统一的IP地址管理工具
                // 添加日志记录以调试
                println("原始avatarUrl: ${responseUser.avatarUrl}")
                
                val processedAvatarUrl = IpAddressManager.processImageUrl(responseUser.avatarUrl)
                println("处理后avatarUrl: $processedAvatarUrl")
                
                val finalUser = responseUser.copy(
                    avatarUrl = processedAvatarUrl,
                    status = responseUser.status ?: "ACTIVE", // 处理null情况，使用默认值
                    isLogin = user.isLogin // 保留登录状态
                )
                dbHelper.addOrUpdateUser(finalUser)
                
                // 更新状态
                user = finalUser
                
                // 显示保存成功提示
                withContext(Dispatchers.Main) {
                    // 显示Toast提示保存成功
                    android.widget.Toast.makeText(context, "保存成功", android.widget.Toast.LENGTH_SHORT).show()
//                    onBack() // 保存成功后返回上一页
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 显示保存失败提示
            } finally {
                isSaving = false
            }
        }
    }
    

    
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            // 添加圆角设计的TopAppBar
                TopAppBar(
                    title = {
                        Text(
                            text = "基本资料",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = ::saveUserInfo,
                            enabled = !isSaving,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(text = "保存", color = MaterialTheme.colorScheme.onPrimary,fontSize = 16.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(237, 237, 237)) // 设置底层背景颜色为RGB(237,237,237)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 白色卡片组件，包含所有用户信息
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary // 设置为纯白色
                )
            ) {
                // 头像上传区域
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .clickable {
                            pickMedia.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // 使用条件判断分别处理不同的头像来源
                        if (avatarUri != null) {
                            AsyncImage(
                                model = avatarUri.toString(),
                                contentDescription = "用户头像",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val avatarUrl = user.avatarUrl
                            if (avatarUrl != null && avatarUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "用户头像",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(id = R.drawable.img),
                                    error = painterResource(id = R.drawable.img)
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.img),
                                    contentDescription = "用户头像",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Text(
                            text = "点击更换头像",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                
                // 表单区域
                Column(modifier = Modifier) {
                    // 用户名
                    ProfileFormItem(label = "用户名", content = {
                        TextField(
                            value = username,
                            onValueChange = { username = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(text = "请输入用户名") },
                            maxLines = 1,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    })

                    ProfileFormItem(label = "昵称", content = {
                        TextField(
                            value = nickname,
                            onValueChange = { nickname = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(text = "请输入昵称") },
                            maxLines = 1,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    })

                    ProfileFormItem(label = "手机号", content = {
                        Text(
                            text = formatPhoneNumber(user.phone),
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Gray
                        )
                    })

                    ProfileFormItem(label = "邮箱", content = {
                        TextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(text = "请输入邮箱") },
                            maxLines = 1,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    })

                    ProfileFormItem(label = "性别", content = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 男性选项
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { gender = "MALE" }
                            ) {
                                RadioButton(
                                    selected = gender == "MALE",
                                    onClick = { gender = "MALE" },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                                Text(text = "男", modifier = Modifier.padding(start = 4.dp))
                            }

                            // 女性选项
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { gender = "FEMALE" }
                            ) {
                                RadioButton(
                                    selected = gender == "FEMALE",
                                    onClick = { gender = "FEMALE" },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                                Text(text = "女", modifier = Modifier.padding(start = 4.dp))
                            }

                            // 保密选项
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { gender = "UNKNOWN" }
                            ) {
                                RadioButton(
                                    selected = gender == "UNKNOWN",
                                    onClick = { gender = "UNKNOWN" },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                                Text(text = "保密", modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    })

                    ProfileFormItem(label = "年龄", showDivider = false, content = {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        showAgeSelector = true
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (age.isNotEmpty()) "${age}岁" else "请选择",
                                    color = if (age.isNotEmpty()) Color.Black else Color.Gray
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "选择年龄",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    })

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // 年龄选择器底部弹出框
        if (showAgeSelector) {
            ModalBottomSheet(
                onDismissRequest = { showAgeSelector = false },
                sheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = false // 允许部分展开
                ),
                containerColor = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // 标题和按钮栏
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAgeSelector = false }) {
                            Text(text = "取消", color = Color.Gray)
                        }
                        Text(text = "选择年龄", fontWeight = FontWeight.Bold)
                        TextButton(onClick = { showAgeSelector = false }) {
                            Text(text = "确定", color = MaterialTheme.colorScheme.primaryContainer)
                        }
                    }

                    // 年龄选择单列布局 - 生成1-100的连续数字选项，每个数字单独占一行
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp), // 限制最大高度
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(100) { index ->
                            val ageNum = index + 1
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        age = ageNum.toString()
                                        showAgeSelector = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ageNum.toString(),
                                    fontSize = 16.sp,
                                    color = if (age == ageNum.toString()) MaterialTheme.colorScheme.primaryContainer else Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileFormItem(label: String, showDivider: Boolean = true, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp), // 固定高度确保间距一致
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.width(80.dp) // 固定标签宽度
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                content()
            }
        }

        // 只有当showDivider为true时才显示分割线
        if (showDivider) {
            HorizontalDivider(
                thickness = 1.dp,
                color = Color(0xFFEEEEEE)
            )
        }
    }
}