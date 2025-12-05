package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


@Suppress("DEPRECATION")
class LoginActivity : ComponentActivity() {
    private val READ_PHONE_PERMISSION = 1001
    private lateinit var dbHelper: DatabaseHelper
    private val devicePhoneNumber = mutableStateOf<String?>(null)
    private var allUserPhones: List<String> = emptyList()
    private val apiService = RetrofitClient.apiService

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
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

    @SuppressLint("HardwareIds")
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



    @Deprecated("Deprecated in Java")
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

    @OptIn(DelicateCoroutinesApi::class)
    private fun handleOneKeyLogin(phoneNumber: String) {
        // 首先根据手机号检查用户是否存在
        val existingUser = dbHelper.getUserByPhone(phoneNumber)
        
        if (existingUser != null) {
            // 本地有记录，执行登录流程
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    // 一键登录可以使用空密码或现有的密码
                    val password = existingUser.password
                    // 调用远程登录接口
                    val loginResponse = apiService.loginUser(
                        mapOf(
                            "phone" to phoneNumber,
                            "password" to password
                        )
                    )
                    
                    if (loginResponse.isSuccess()) {
                        // 登录成功，更新本地用户信息
                        val loggedInUser = loginResponse.data ?: existingUser
                        // 处理avatarUrl前缀，使用统一的IP地址管理工具
                        val processedAvatarUrl = IpAddressManager.processImageUrl(loggedInUser.avatarUrl)
                        
                        val finalUser = loggedInUser.copy(
                            isLogin = true,
                            avatarUrl = processedAvatarUrl,
                            password = password // 保持现有密码
                        )
                        dbHelper.addOrUpdateUser(finalUser)
                        
                        runOnUiThread {
                            navigateToMain()
                        }
                    } else {
                        // 登录失败，可能需要重新注册
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "登录失败，尝试重新注册", Toast.LENGTH_SHORT).show()
                        }
                        // 执行注册流程
                        registerNewUser(phoneNumber, existingUser.password)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "一键登录远程API调用失败")
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "登录失败，请检查网络连接", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            // 本地无记录，执行注册/登录检查流程
            checkPhoneRegistration(phoneNumber, "") // 一键登录使用空密码
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun handleManualLogin(phone: String, password: String) {
        val encryptedPassword = encryptPassword(password)
        val user = dbHelper.checkUser(phone, encryptedPassword)
        
        if (user != null) {
            // 本地有记录，执行密码登录流程
            // 调用远程登录接口
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    // 调用远程登录接口
                    val loginResponse = apiService.loginUser(
                        mapOf(
                            "phone" to phone,
                            "password" to encryptedPassword
                        )
                    )
                    
                    if (loginResponse.isSuccess()) {
                        // 登录成功，更新本地用户信息
                        val loggedInUser = loginResponse.data ?: user
                        // 处理avatarUrl前缀，使用统一的IP地址管理工具
                        val processedAvatarUrl = IpAddressManager.processImageUrl(loggedInUser.avatarUrl)
                        
                        val finalUser = loggedInUser.copy(
                            isLogin = true,
                            avatarUrl = processedAvatarUrl,
                            password = encryptedPassword // 确保密码是加密后的
                        )
                        dbHelper.addOrUpdateUser(finalUser)
                        
                        runOnUiThread {
                            navigateToMain()
                        }
                    } else {
                        // 登录失败
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, loginResponse.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "远程登录API调用失败")
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "登录失败，请检查网络连接", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            // 本地无记录，执行注册/登录检查流程
            checkPhoneRegistration(phone, encryptedPassword)
        }
    }
    
    /**
     * 检查手机号在远程数据库中的注册状态
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun checkPhoneRegistration(phone: String, password: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // 调用远程手机号注册状态检查接口
                val checkResponse = apiService.checkPhoneRegistration(phone)
                
                if (checkResponse.isSuccess()) {
                    val exists = checkResponse.data?.get("exists") == true
                    
                    if (exists) {
                        // 手机号已注册，执行登录流程
                        val loginResponse = apiService.loginUser(
                            mapOf(
                                "phone" to phone,
                                "password" to password
                            )
                        )
                        
                        if (loginResponse.isSuccess()) {
                            // 登录成功，保存到本地数据库
                            val loggedInUser = loginResponse.data
                            if (loggedInUser != null) {
                                // 处理avatarUrl前缀，使用统一的IP地址管理工具
                                val processedAvatarUrl = IpAddressManager.processImageUrl(loggedInUser.avatarUrl)
                                
                                val finalUser = loggedInUser.copy(
                                    isLogin = true,
                                    avatarUrl = processedAvatarUrl,
                                    password = password
                                )
                                dbHelper.addOrUpdateUser(finalUser)
                                
                                runOnUiThread {
                                    navigateToMain()
                                }
                            }
                        } else {
                            // 登录失败
                            runOnUiThread {
                                Toast.makeText(this@LoginActivity, loginResponse.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        // 手机号未注册，执行注册流程
                        registerNewUser(phone, password)
                    }
                } else {
                    // 检查失败
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, checkResponse.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "检查手机号注册状态API调用失败")
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "检查失败，请检查网络连接", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    /**
     * 注册新用户
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun registerNewUser(phone: String, password: String) {
        val username = "用户" + phone.substring(phone.length - 6) // 使用手机号后6位作为用户名
        
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // 调用远程注册接口
                val newUser = User(
                    phone = phone,
                    password = password,
                    username = username
                )
                val registerResponse = apiService.registerUser(newUser)
                
                if (registerResponse.isSuccess()) {
                        // 注册成功，保存到本地数据库
                        val registeredUser = registerResponse.data
                        if (registeredUser != null) {
                            // 处理avatarUrl前缀，使用统一的IP地址管理工具
                            val processedAvatarUrl = IpAddressManager.processImageUrl(registeredUser.avatarUrl)
                            
                            val finalUser = registeredUser.copy(
                                isLogin = true,
                                avatarUrl = processedAvatarUrl,
                                password = password
                            )
                            dbHelper.addOrUpdateUser(finalUser)
                        
                        runOnUiThread {
                            navigateToMain()
                        }
                    }
                } else {
                    // 注册失败
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, registerResponse.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "远程注册API调用失败")
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "注册失败，请检查网络连接", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
    
    /**
     * MD5加密函数
     */
    private fun encryptPassword(password: String): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val hashBytes = md.digest(password.toByteArray())
            val stringBuilder = StringBuilder()
            for (byte in hashBytes) {
                val hex = Integer.toHexString(0xFF and byte.toInt())
                if (hex.length == 1) {
                    stringBuilder.append('0')
                }
                stringBuilder.append(hex)
            }
            stringBuilder.toString()
        } catch (e: NoSuchAlgorithmException) {
            Timber.e("MD5加密失败: ${e.message}")
            password // 加密失败时返回原密码
        }
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
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    // 添加对话框显示状态
    var showAgreementDialog by remember { mutableStateOf(false) }

    // 如果有设备号码，自动填充（脱敏）
    val displayedPhoneNumber = devicePhoneNumber ?: ""
    
    // 检查本地是否有当前手机号的记录
    val dbHelper = remember { DatabaseHelper(context) }
    val hasLocalRecord = remember(phoneNumber) {
        if (phoneNumber.isNotEmpty()) {
            dbHelper.getUserByPhone(phoneNumber) != null
        } else {
            displayedPhoneNumber.isNotEmpty() && dbHelper.getUserByPhone(displayedPhoneNumber) != null
        }
    }

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
            Surface(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(100.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.title),
                    contentDescription = "应用图标",
                    modifier = Modifier.fillMaxSize()
                )
            }
            
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
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
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
            if (displayedPhoneNumber.isEmpty() || showManualLogin || hasLocalRecord) {
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { 
                        phoneNumber = it 
                        // 清除错误信息
                        errorMessage = ""
                    },
                    label = { Text("手机号") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it 
                        // 清除错误信息
                        errorMessage = ""
                    },
                    label = { Text("密码") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                // 显示错误信息
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        if (isAgreed) {
                            isLoading = true
                            onManualLogin(phoneNumber, password)
                            isLoading = false
                        } else {
                            // 设置对话框显示状态为true
                            showAgreementDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    enabled = phoneNumber.isNotEmpty() && password.isNotEmpty() && !isLoading
                ) {
                    if (isLoading) {
                        Text(
                            text = "登录中...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "登录",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
                        // 清除错误信息
                        errorMessage = ""
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