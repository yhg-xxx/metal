package com.example.ui.features

import android.annotation.SuppressLint
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
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
import com.example.model.BaseResponse
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
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.ExperimentalMaterial3Api



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
class ProfileEditActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private val apiService = RetrofitClient.apiService

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DatabaseHelper(this)

        // 关键修改：确保内容不会延伸到状态栏区域
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 设置状态栏透明 - 使用 Android 的颜色常量而不是 Compose 的 Color
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        // 获取窗口插入控制器
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        // 设置状态栏文字为深色（适用于浅色背景）
        windowInsetsController.isAppearanceLightStatusBars = true

        // 显示状态栏
        windowInsetsController.show(androidx.core.view.WindowInsetsCompat.Type.statusBars())

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
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    
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
                            "age": ${if (updatedUser.age != null) updatedUser.age else "null"}
                        }
                    """.trimIndent()
                    
                    val userMediaType = "application/json".toMediaTypeOrNull()
                    val userRequestBody = userMediaType?.let {
                        RequestBody.create(it, userJson)
                    }
                    
                    // 准备头像文件
                    val avatarPart = avatarUri?.let { uri ->
                        val file = createTempFileFromUri(context, uri)
                        val fileMediaType = (context.contentResolver.getType(uri) ?: "image/jpeg").toMediaTypeOrNull()
                        val requestFile = fileMediaType?.let {
                            RequestBody.create(it, file)
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
                        BaseResponse(200, "success", updatedUser)
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
            .fillMaxSize()
            .statusBarsPadding(), // 添加状态栏内边距
        topBar = {
            // 使用与首页一致的TopAppBar实现
            TopAppBar(
                title = {
                    Text(
                        text = "编辑个人资料",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = ::saveUserInfo,
                        enabled = !isSaving
                    ) {
                        Text(text = "保存", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F7F7))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 头像上传区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
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
                    androidx.compose.material3.Text(
                        text = "点击更换头像",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 表单区域
            Column(modifier = Modifier.background(Color.White)) {
                // 用户名
                FormItem(label = "用户名") {
                    TextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { androidx.compose.material3.Text(text = "请输入用户名") },
                        maxLines = 1,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
                
                // 昵称
                FormItem(label = "昵称") {
                    TextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { androidx.compose.material3.Text(text = "请输入昵称") },
                        maxLines = 1,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
                
                // 手机号（不可编辑）
                FormItem(label = "手机号") {
                    androidx.compose.material3.Text(
                        text = user.phone,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Gray
                    )
                }
                
                // 邮箱
                FormItem(label = "邮箱") {
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { androidx.compose.material3.Text(text = "请输入邮箱") },

                        maxLines = 1,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
                
                // 性别
                FormItem(label = "性别") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        RadioButton(
                            selected = gender == "MALE",
                            onClick = { gender = "MALE" },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFF5A67D8)
                            )
                        )
                        androidx.compose.material3.Text(text = "男", modifier = Modifier.clickable { gender = "MALE" })
                        
                        RadioButton(
                            selected = gender == "FEMALE",
                            onClick = { gender = "FEMALE" },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFF5A67D8)
                            )
                        )
                        androidx.compose.material3.Text(text = "女", modifier = Modifier.clickable { gender = "FEMALE" })
                        
                        RadioButton(
                            selected = gender == "UNKNOWN",
                            onClick = { gender = "UNKNOWN" },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFF5A67D8)
                            )
                        )
                        androidx.compose.material3.Text(text = "保密", modifier = Modifier.clickable { gender = "UNKNOWN" })
                    }
                }
                
                // 年龄
                FormItem(label = "年龄") {
                    TextField(
                        value = age,
                        onValueChange = { newValue -> 
                            age = if (newValue.matches(Regex("\\d*"))) newValue else age 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { androidx.compose.material3.Text(text = "请输入年龄") },

                        maxLines = 1,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormItem(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        androidx.compose.material3.Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
    androidx.compose.material3.Divider(modifier = Modifier.height(1.dp).background(Color(0xFFEEEEEE)))
}