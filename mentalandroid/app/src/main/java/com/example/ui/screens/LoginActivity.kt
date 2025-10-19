package com.example.ui.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.model.User
import com.example.network.RetrofitClient
import com.example.ui.theme.MentalTheme
import com.example.util.DatabaseHelper
import com.example.util.IpAddressManager
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import timber.log.Timber


class LoginActivity : ComponentActivity() {
    private val READ_PHONE_PERMISSION = 1001
    private lateinit var dbHelper: DatabaseHelper
    private val devicePhoneNumber = mutableStateOf<String?>(null)
    private var allUserPhones: List<String> = emptyList()
    private val apiService = RetrofitClient.apiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DatabaseHelper(this)
        
        // 获取所有已登录过的用户手机号
        allUserPhones = dbHelper.getAllUserPhones()
        
        // 检查是否有预填充的手机号
        val prefilledPhone = intent.getStringExtra("PREFILLED_PHONE") ?: ""
        val isManualLogin = intent.getBooleanExtra("IS_MANUAL_LOGIN", false)
        
        // 尝试获取本机号码
        checkPhonePermission()
        
        setContent {
            MentalTheme {
                LoginScreen(
                    devicePhoneNumber = devicePhoneNumber.value,
                    allUserPhones = allUserPhones,
                    prefilledPhone = prefilledPhone,
                    isManualLogin = isManualLogin,
                    onOneKeyLogin = { phoneNumber ->
                        handleOneKeyLogin(phoneNumber)
                    },
                    onManualLogin = { phone, password ->
                        handleManualLogin(phone, password)
                    },
                    onHistoryPhoneSelected = { phone ->
                        // 设置选中的历史手机号并切换到手动登录模式
                        startActivity(Intent(this, LoginActivity::class.java).apply {
                            putExtra("PREFILLED_PHONE", phone)
                            putExtra("IS_MANUAL_LOGIN", true)
                        })
                        finish()
                    }
                )
            }
        }
    }

    private fun checkPhonePermission() {
        // 检查所有需要的权限
        val permissions = arrayOf(
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.READ_PHONE_NUMBERS,
            android.Manifest.permission.READ_SMS
        )
        
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                READ_PHONE_PERMISSION
            )
        } else {
            getDevicePhoneNumber()
        }
    }

    private fun getDevicePhoneNumber() {
        try {
            val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                devicePhoneNumber.value = telephonyManager.line1Number
                // 有些运营商的手机号码可能带有国家代码，如+86，这里可以根据需要处理
                if (!devicePhoneNumber.value.isNullOrEmpty() && devicePhoneNumber.value!!.startsWith("+86")) {
                    devicePhoneNumber.value = devicePhoneNumber.value!!.substring(3)
                }

            }
        } catch (e: Exception) {
            Timber.e(e, "获取本机号码失败")
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_PHONE_PERMISSION) {
            // 检查是否有任何一个权限被授予
            val hasAnyPermission = grantResults.any { it == PackageManager.PERMISSION_GRANTED }
            
            if (hasAnyPermission) {
                getDevicePhoneNumber()
            } else {
                Toast.makeText(this, "无法获取本机号码，您可以手动输入或选择历史登录账号", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleOneKeyLogin(phoneNumber: String) {
        // 首先根据手机号检查用户是否存在
        val existingUser = dbHelper.getUserByPhone(phoneNumber)
        if (existingUser != null) {
            // 用户已存在，更新登录状态
            val updatedUser = existingUser.copy(isLogin = true)
            dbHelper.addOrUpdateUser(updatedUser)
            
            // 异步调用API更新用户信息
            GlobalScope.launch(Dispatchers.IO) {
                try {
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
                    """
                    
                    val userMediaType = "application/json".toMediaTypeOrNull()
                    val userRequestBody = userMediaType?.let {
                        RequestBody.create(it, userJson)
                    } ?: throw IllegalStateException("Invalid media type")
                    
                    // 调用API更新用户
                    val apiResponse = apiService.updateUser(
                        phone = phoneNumber,
                        user = userRequestBody
                    )
                    
                    // 从响应中获取用户数据
                    val responseUser = apiResponse.data ?: updatedUser
                    
                    // 处理avatarUrl前缀，使用统一的IP地址管理工具
                    val processedAvatarUrl = IpAddressManager.processImageUrl(responseUser.avatarUrl)
                    
                    // 更新本地用户信息
                    val finalUser = responseUser.copy(
                        isLogin = true,
                        avatarUrl = processedAvatarUrl
                    )
                    dbHelper.addOrUpdateUser(finalUser)
                } catch (e: Exception) {
                    Timber.e(e, "更新用户API调用失败")
                }
            }
            
            // 跳转到主页
            navigateToMain()
        } else {
            // 用户不存在，创建新用户
            val newUser = User(
                username = "用户" + (10000..99999).random(),
                phone = phoneNumber,
                password = "", // 一键登录可以不设置密码
                isLogin = true
            )

            // 先保存到本地数据库
            dbHelper.addOrUpdateUser(newUser)
            
            // 然后异步调用API保存到远端服务器
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    // 准备用户信息JSON字符串
                    val userJson = """
                        {
                            "username": "${newUser.username}",
                            "password": "${newUser.password}",
                            "phone": "${newUser.phone}",
                            "email": null,
                            "nickname": null,
                            "gender": "${newUser.gender}",
                            "age": null
                        }
                    """.trimIndent()
                    
                    val userMediaType = "application/json".toMediaTypeOrNull()
                    val userRequestBody = userMediaType?.let {
                        RequestBody.create(it, userJson)
                    } ?: throw IllegalStateException("Invalid media type")
                    
                    // 调用API创建用户
                    val apiResponse = apiService.createUser(user = userRequestBody)
                    
                    // 从响应中获取用户数据
                    val responseUser = apiResponse.data ?: newUser
                    
                    // 更新本地用户信息
                    val updatedLocalUser = responseUser.copy(
                        isLogin = true
                    )
                    dbHelper.addOrUpdateUser(updatedLocalUser)
                } catch (e: Exception) {
                    Timber.e(e, "创建用户API调用失败")
                }
            }
            
            navigateToMain()
        }
    }

    private fun handleManualLogin(phone: String, password: String) {
        val user = dbHelper.checkUser(phone, password)
        if (user != null) {
            // 用户存在，更新登录状态
            val updatedUser = user.copy(isLogin = true)
            dbHelper.addOrUpdateUser(updatedUser)
            
            // 异步调用API更新用户信息
            GlobalScope.launch(Dispatchers.IO) {
                try {
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
                    } ?: throw IllegalStateException("Invalid media type")
                    
                    // 调用API更新用户
                    val apiResponse = apiService.updateUser(
                        phone = phone,
                        user = userRequestBody
                    )
                    
                    // 从响应中获取用户数据
                    val responseUser = apiResponse.data ?: updatedUser
                    
                    // 更新本地用户信息
                    val finalUser = responseUser.copy(
                        isLogin = true
                    )
                    dbHelper.addOrUpdateUser(finalUser)
                } catch (e: Exception) {
                    Timber.e(e, "更新用户API调用失败")
                }
            }
            
            navigateToMain()
        } else {
            // 用户不存在，创建新用户
            val newUser = User(
                username = "用户" + (10000..99999).random(),
                phone = phone,
                password = password,
                isLogin = true
            )
            
            // 先保存到本地数据库
            dbHelper.addOrUpdateUser(newUser)
            
            // 然后异步调用API保存到远端服务器
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    // 准备用户信息JSON字符串
                    val userJson = """
                        {
                            "username": "${newUser.username}",
                            "password": "${newUser.password}",
                            "phone": "${newUser.phone}",
                            "email": null,
                            "nickname": null,
                            "gender": "${newUser.gender}",
                            "age": null
                        }
                    """.trimIndent()
                    
                    val userMediaType = "application/json".toMediaTypeOrNull()
                    val userRequestBody = userMediaType?.let {
                        RequestBody.create(it, userJson)
                    } ?: throw IllegalStateException("Invalid media type")
                    
                    // 调用API创建用户
                    val apiResponse = apiService.createUser(user = userRequestBody)
                    
                    // 从响应中获取用户数据
                    val responseUser = apiResponse.data ?: newUser
                    
                    // 更新本地用户信息
                    val updatedLocalUser = responseUser.copy(
                        isLogin = true
                    )
                    dbHelper.addOrUpdateUser(updatedLocalUser)
                } catch (e: Exception) {
                    Timber.e(e, "创建用户API调用失败")
                }
            }
            
            navigateToMain()
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}



@Composable
fun LoginScreen(
    devicePhoneNumber: String?,
    allUserPhones: List<String>,
    prefilledPhone: String = "",
    isManualLogin: Boolean = false,
    onOneKeyLogin: (String) -> Unit,
    onManualLogin: (String, String) -> Unit,
    onHistoryPhoneSelected: (String) -> Unit
) {
    var phoneNumber by remember { mutableStateOf(prefilledPhone) }
    var password by remember { mutableStateOf("") }
    var isAgreed by remember { mutableStateOf(false) }
    var showManualLogin by remember { mutableStateOf(isManualLogin) }
    var showHistoryPhones by remember { mutableStateOf(false) }
    val context = LocalContext.current
    // 添加对话框显示状态
    var showAgreementDialog by remember { mutableStateOf(false) }

    // 如果有设备号码，自动填充（脱敏）
    val displayedPhoneNumber = devicePhoneNumber ?: ""

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4FF))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 应用图标
            Image(
                painter = painterResource(id = R.drawable.img),
                contentDescription = "应用图标",
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFF5A67D8), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // 手机号显示
            if (displayedPhoneNumber.isNotEmpty() && !isManualLogin) {
                Text(
                    text = displayedPhoneNumber,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 本机号码一键登录按钮
                Button(
                    onClick = {
                        if (isAgreed) {
                            onOneKeyLogin(displayedPhoneNumber)
                        } else {
                            // 设置对话框显示状态为true
                            showAgreementDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    // 始终启用按钮，即使未勾选协议
                    enabled = true
                ) {
                    Text(
                        text = "本机号码一键登录",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // 手动登录表单
            if (displayedPhoneNumber.isEmpty() || showManualLogin) {
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("手机号") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        if (isAgreed) {
                            onManualLogin(phoneNumber, password)
                        } else {
                            // 设置对话框显示状态为true
                            showAgreementDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    // 只检查手机号和密码是否为空，不检查协议是否勾选
                    enabled = phoneNumber.isNotEmpty() && password.isNotEmpty()
                ) {
                    Text(
                        text = "登录",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 切换登录方式
            if (displayedPhoneNumber.isNotEmpty()) {
                Text(
                    text = if (showManualLogin) "使用本机号码登录" else "其他手机号码登录",
                    fontSize = 14.sp,
                    color = Color(0xFF5A67D8),
                    modifier = Modifier.clickable {
                        showManualLogin = !showManualLogin
                    }
                )
            }
            
            // 显示历史登录手机号选项
            if (allUserPhones.isNotEmpty() && !showManualLogin && displayedPhoneNumber.isNotEmpty()) {
                Text(
                    text = if (showHistoryPhones) "隐藏历史账号" else "选择历史账号",
                    fontSize = 14.sp,
                    color = Color(0xFF5A67D8),
                    modifier = Modifier.clickable {
                        showHistoryPhones = !showHistoryPhones
                    }
                )
                
                // 显示历史手机号列表
                    if (showHistoryPhones) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 过滤掉当前显示的设备号码
                        val filteredPhones = allUserPhones.filter { it != displayedPhoneNumber }
                        
                        if (filteredPhones.isNotEmpty()) {
                            Column {
                                filteredPhones.forEachIndexed { index, phone ->
                                    val maskedPhone = phone
                                    val dbHelper = remember { DatabaseHelper(context) }
                                    val user = remember { dbHelper.getUserByPhone(phone) }
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onHistoryPhoneSelected(phone)
                                            }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (user?.avatarUrl != null && user.avatarUrl.isNotEmpty()) {
                                            AsyncImage(
                                                model = user.avatarUrl,
                                                contentDescription = "用户头像",
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .padding(4.dp),
                                                placeholder = painterResource(id = R.drawable.img),
                                                error = painterResource(id = R.drawable.img)
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(id = R.drawable.img),
                                                contentDescription = "用户头像",
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .padding(4.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = maskedPhone,
                                            fontSize = 14.sp,
                                            color = Color(0xFF333333)
                                        )
                                    }
                                    
                                    // 最后一个不显示分割线
                                    if (index < filteredPhones.size - 1) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0xFFEEEEEE)))
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "暂无其他历史账号",
                                fontSize = 14.sp,
                                color = Color(0xFF999999),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 协议同意复选框
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = isAgreed,
                    onCheckedChange = { isAgreed = it }
                )
                
                Text(
                    text = "同意《中国移动认证服务条款》和《用户协议》和《隐私政策》",
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
            }
            
            // Compose的AlertDialog组件
            if (showAgreementDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showAgreementDialog = false },
                    title = { Text("提示") },
                    text = { Text("请先同意《中国移动认证服务条款》和《用户协议》和《隐私政策》") },
                    confirmButton = {
                        Button(
                            onClick = { showAgreementDialog = false }
                        ) {
                            Text("确定")
                        }
                    }
                )
            }
        }
    }
}