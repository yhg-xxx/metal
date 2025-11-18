package com.example.ui.features

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background

import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.Child
import com.example.model.CreateChildRequest
import com.example.model.UpdateChildRequest
import com.example.network.ApiResponse
import com.example.network.RetrofitClient
import com.example.ui.theme.MentalTheme
import com.example.util.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChildInfoScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MentalTheme {
                ChildInfoScreen(
                    onBackClick = { finish() }
                )
            }
        }
    }
}

// 表单项目组件
@Composable
fun FormItem(label: String, isRequired: Boolean = false, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            if (isRequired) {
                Text(
                    text = "*",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
        content()
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),

        )
    }
}

// 性别选择组件 - 优化版
@Composable
fun GenderOption(
    text: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)

    // 动态颜色
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.surfaceVariant
    val selectedTextColor = MaterialTheme.colorScheme.onPrimary
    val unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    // 动画效果
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.95f else 1f,
        animationSpec = tween(durationMillis = 150)
    )

    val elevation by animateFloatAsState(
        targetValue = if (isSelected) 4.dp.value else 2.dp.value,
        animationSpec = tween(durationMillis = 150)
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = elevation.dp,
                shape = shape,
                clip = false
            )
            .clip(shape)
    ) {
        Surface(
            shape = shape,
            color = if (isSelected) selectedColor else unselectedColor,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect() },
            tonalElevation = if (isSelected) 3.dp else 1.dp
        ) {
            Text(
                text = text,
                color = if (isSelected) selectedTextColor else unselectedTextColor,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth()
            )
        }
    }
}

// 性别选择组组件
@Composable
fun GenderSelection(
    selectedGender: String,
    onGenderSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val genders = listOf("男", "女")

    Column(
        modifier = modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            genders.forEach { gender ->
                GenderOption(
                    text = gender,
                    isSelected = selectedGender == gender,
                    onSelect = { onGenderSelected(gender) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 验证提示
        if (selectedGender.isEmpty()) {
            Text(
                text = "请选择性别",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildInfoScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // 状态管理
    var currentChild by remember { mutableStateOf<Child?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var isAdding by remember { mutableStateOf(false) } // 新增状态，区分是添加还是编辑
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf("") }

    // 新增：所有孩子列表和下拉菜单状态
    var childrenList by remember { mutableStateOf<List<Child>>(emptyList()) }
    var showChildrenDropdown by remember { mutableStateOf(false) }
    
    // 日期选择器状态
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // 表单字段状态
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var birthYearMonth by remember { mutableStateOf("") }
    var ethnicity by remember { mutableStateOf("") }
    var currentSchool by remember { mutableStateOf("") }
    // 扩展字段
    var householdRegister by remember { mutableStateOf("") }
    var birthOrder by remember { mutableStateOf("") }
    var birthPlace by remember { mutableStateOf("") }
    var languageEnvironment by remember { mutableStateOf("") }
    var homeAddress by remember { mutableStateOf("") }
    var habits by remember { mutableStateOf("") }
    var interestActivities by remember { mutableStateOf("") }
    var healthStatus by remember { mutableStateOf("") }
    var healthDescription by remember { mutableStateOf("") }
    var pastIllness by remember { mutableStateOf("") }
    // 新增：自定义过往病史输入
    var customPastIllness by remember { mutableStateOf("") }
    var fatherPhone by remember { mutableStateOf("") }
    var motherPhone by remember { mutableStateOf("") }
    var guardianPhone by remember { mutableStateOf("") }

    // 重置表单函数
    fun resetForm() {
        name = ""
        gender = ""
        birthYearMonth = ""
        ethnicity = ""
        currentSchool = ""
        householdRegister = ""
        birthOrder = ""
        birthPlace = ""
        languageEnvironment = ""
        homeAddress = ""
        habits = ""
        interestActivities = ""
        healthStatus = ""
        healthDescription = ""
        pastIllness = ""
        customPastIllness = ""
        fatherPhone = ""
        motherPhone = ""
        guardianPhone = ""
    }

    // 填充表单数据函数
    fun fillFormWithChildData(child: Child) {
        name = child.name
        gender = child.gender
        birthYearMonth = child.birthYearMonth ?: ""
        ethnicity = child.ethnicity ?: ""
        currentSchool = child.currentSchool ?: ""
        householdRegister = child.householdRegister ?: ""
        birthOrder = child.birthOrder ?: ""
        birthPlace = child.birthPlace ?: ""
        languageEnvironment = child.languageEnvironment ?: ""
        homeAddress = child.homeAddress ?: ""
        habits = child.habits ?: ""
        interestActivities = child.interestActivities ?: ""
        healthStatus = child.healthStatus ?: ""
        healthDescription = child.healthDescription ?: ""
        
        // 检查是否为自定义病史（不在预设选项中的值）
        val dbPastIllness = child.pastIllness ?: ""
        // 预设的过往病史选项（需要和UI中的选项保持一致）
        val standardPastIllnessOptions = listOf("NONE", "ASTHMA", "ALLERGY", "DIABETES", "HEART_DISEASE", "OTHER")
        
        if (standardPastIllnessOptions.contains(dbPastIllness)) {
            // 标准选项，直接使用
            pastIllness = dbPastIllness
            customPastIllness = ""
        } else if (dbPastIllness.isNotEmpty()) {
            // 自定义输入，设置为OTHER并恢复输入内容
            pastIllness = "OTHER"
            customPastIllness = dbPastIllness
        } else {
            pastIllness = ""
            customPastIllness = ""
        }
        fatherPhone = child.fatherPhone ?: ""
        motherPhone = child.motherPhone ?: ""
        guardianPhone = child.guardianPhone ?: ""
    }

    // 修改：加载孩子信息的函数，同时获取所有孩子列表
    suspend fun loadChildInfo() {
        isLoading = true
        errorMessage = null
        try {
            val dbHelper = DatabaseHelper(context)
            val loggedInUser = dbHelper.getLoggedInUser()

            if (loggedInUser != null) {
                val childApiService = RetrofitClient.getChildApiService()

                // 同时获取当前操作孩子和所有孩子列表
                val currentChildResponse = childApiService.getCurrentChild(userId = loggedInUser.id.toLong())
                val childrenListResponse = childApiService.getChildrenByUserId(userId = loggedInUser.id.toLong())

                if (currentChildResponse.isSuccess() && currentChildResponse.data != null) {
                    currentChild = currentChildResponse.data
                    fillFormWithChildData(currentChildResponse.data)
                    isEditing = false
                    isAdding = false
                } else {
                    if (currentChildResponse.code == 404 || currentChildResponse.message.contains("未设置当前操作孩子")) {
                        resetForm()
                        isEditing = false
                        isAdding = false
                        currentChild = null
                    } else {
                        errorMessage = "获取孩子信息失败：${currentChildResponse.message}"
                    }
                }

                // 处理所有孩子列表
                if (childrenListResponse.isSuccess() && childrenListResponse.data != null) {
                    childrenList = childrenListResponse.data
                } else {
                    // 如果获取列表失败，但不影响主要功能，只记录错误
                    println("获取孩子列表失败：${childrenListResponse.message}")
                }
            } else {
                errorMessage = "未找到登录用户信息"
            }
        } catch (e: Exception) {
            errorMessage = "加载数据时出错：${e.message}"
        } finally {
            isLoading = false
        }
    }
    // 新增：切换当前操作孩子的函数
    suspend fun switchCurrentChild(childId: Long) {
        try {
            val dbHelper = DatabaseHelper(context)
            val loggedInUser = dbHelper.getLoggedInUser()

            if (loggedInUser != null) {
                val childApiService = RetrofitClient.getChildApiService()
                val response = childApiService.setCurrentChild(
                    userId = loggedInUser.id.toLong(),
                    childId = childId
                )

                if (response.isSuccess()) {
                    // 重新加载孩子信息
                    loadChildInfo()
                    showToast = "切换孩子成功"
                } else {
                    showToast = "切换孩子失败：${response.message}"
                }
            } else {
                showToast = "未找到登录用户信息"
            }
        } catch (e: Exception) {
            showToast = "切换孩子失败：${e.message}"
        }
    }

    // 获取当前操作孩子信息
    LaunchedEffect(Unit) {
        loadChildInfo()
    }

    // 验证表单
    fun validateForm(): Boolean {
        if (name.isBlank()) {
            showToast = "孩子姓名不能为空"
            return false
        }
        if (gender.isBlank()) {
            showToast = "性别不能为空"
            return false
        }
        // 添加健康状态验证逻辑：当选择较差(POOR)或很差(VERY_POOR)时，必须填写健康描述
        if ((healthStatus == "POOR" || healthStatus == "VERY_POOR") && healthDescription.isBlank()) {
            showToast = "当健康状态为'较差'或'很差'时，必须填写健康状态描述"
            return false
        }
        return true
    }

    // 保存孩子信息（添加或更新）
    suspend fun saveChildInfo() {
        if (!validateForm() || isSaving) return

        isSaving = true
        try {
            // 获取当前登录用户信息
            val dbHelper = DatabaseHelper(context)
            val loggedInUser = dbHelper.getLoggedInUser()

            if (loggedInUser == null) {
                showToast = "未找到登录用户信息，请重新登录"
                return
            }

            // 使用RetrofitClient获取ChildApiService实例
            val childApiService = RetrofitClient.getChildApiService()

            if (isAdding) {
                // 添加新孩子
                    // 当过往病史选择"其他"时，将customPastIllness的值保存到pastIllness字段
                    val finalPastIllness = if (pastIllness == "OTHER" && customPastIllness.isNotEmpty()) {
                        customPastIllness
                    } else {
                        pastIllness
                    }
                    
                    val request = CreateChildRequest(
                        userId = loggedInUser.id.toLong(),
                        name = name,
                        gender = gender,
                        birthYearMonth = birthYearMonth.ifEmpty { null },
                        ethnicity = ethnicity.ifEmpty { null },
                        currentSchool = currentSchool.ifEmpty { null },
                        householdRegister = householdRegister.ifEmpty { null },
                        birthOrder = birthOrder.ifEmpty { null },
                        birthPlace = birthPlace.ifEmpty { null },
                        languageEnvironment = languageEnvironment.ifEmpty { null },
                        homeAddress = homeAddress.ifEmpty { null },
                        habits = habits.ifEmpty { null },
                        interestActivities = interestActivities.ifEmpty { null },
                        healthStatus = healthStatus.ifEmpty { null },
                        healthDescription = healthDescription.ifEmpty { null },
                        pastIllness = finalPastIllness.ifEmpty { null },
                        fatherPhone = fatherPhone.ifEmpty { null },
                        motherPhone = motherPhone.ifEmpty { null },
                        guardianPhone = guardianPhone.ifEmpty { null }
                    )

                val response: ApiResponse<Child> = childApiService.createChild(request)

                if (response.isSuccess()) {
                    // 如果有返回数据，则更新状态
                    if (response.data != null) {
                        // 设置为当前操作孩子
                        childApiService.setCurrentChild(
                            userId = loggedInUser.id.toLong(),
                            childId = response.data.id
                        )
                    }
                    isEditing = false
                    isAdding = false
                    showToast = "孩子信息添加成功"
                    // 自动刷新数据以显示最新信息
                    loadChildInfo()
                } else {
                    showToast = "添加孩子信息失败：${response.message}"
                }
            } else {
                // 更新孩子信息
                    // 当过往病史选择"其他"时，将customPastIllness的值保存到pastIllness字段
                    val finalPastIllness = if (pastIllness == "OTHER" && customPastIllness.isNotEmpty()) {
                        customPastIllness
                    } else {
                        pastIllness
                    }
                    
                    val request = UpdateChildRequest(
                        id = currentChild!!.id,
                        name = name,
                        gender = gender,
                        birthYearMonth = birthYearMonth.ifEmpty { null },
                        ethnicity = ethnicity.ifEmpty { null },
                        currentSchool = currentSchool.ifEmpty { null },
                        householdRegister = householdRegister.ifEmpty { null },
                        birthOrder = birthOrder.ifEmpty { null },
                        birthPlace = birthPlace.ifEmpty { null },
                        languageEnvironment = languageEnvironment.ifEmpty { null },
                        homeAddress = homeAddress.ifEmpty { null },
                        habits = habits.ifEmpty { null },
                        interestActivities = interestActivities.ifEmpty { null },
                        healthStatus = healthStatus.ifEmpty { null },
                        healthDescription = healthDescription.ifEmpty { null },
                        pastIllness = finalPastIllness.ifEmpty { null },
                        fatherPhone = fatherPhone.ifEmpty { null },
                        motherPhone = motherPhone.ifEmpty { null },
                        guardianPhone = guardianPhone.ifEmpty { null }
                    )

                val response: ApiResponse<Child> = childApiService.updateChild(request)

                if (response.isSuccess()) {
                    isEditing = false
                    isAdding = false
                    showToast = "孩子信息修改成功"
                    // 自动刷新数据以显示最新信息
                    loadChildInfo()
                } else {
                    showToast = "更新孩子信息失败：${response.message}"
                }
            }
        } catch (e: Exception) {
            showToast = "保存孩子信息失败：${e.message}"
        } finally {
            isSaving = false
        }
    }

    // 开始添加新孩子
    fun startAddingChild() {
        resetForm()
        isEditing = true
        isAdding = true
    }

    // 开始编辑当前孩子
    fun startEditing() {
        if (currentChild != null) {
            fillFormWithChildData(currentChild!!)
            isEditing = true
            isAdding = false
        }
    }

    // 取消编辑/添加
    fun cancelEditing() {
        if (currentChild != null) {
            fillFormWithChildData(currentChild!!)
        } else {
            resetForm()
        }
        isEditing = false
        isAdding = false
    }

    // 显示Toast消息
    LaunchedEffect(showToast) {
        if (showToast.isNotEmpty()) {
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(context, showToast, android.widget.Toast.LENGTH_SHORT).show()
                showToast = ""
            }
        }
    }

    MentalTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when {
                                isAdding -> "新增孩子"
                                isEditing -> "编辑孩子"
                                else -> "孩子信息"
                            },
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    actions = {
                        if (isEditing) {
                            // 编辑/添加模式下显示保存和取消按钮
                            Row {
                                TextButton(
                                    onClick = { cancelEditing() },
                                    enabled = !isSaving
                                ) {
                                    Text("取消", color = MaterialTheme.colorScheme.onPrimary)
                                }
                                TextButton(
                                    onClick = { coroutineScope.launch { saveChildInfo() } },
                                    enabled = !isSaving
                                ) {
                                    Text("保存", color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        } else {
                            // 查看模式下显示添加和编辑按钮
                            Row {
                                // 添加孩子按钮
                                IconButton(
                                    onClick = { startAddingChild() }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "添加孩子",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                // 编辑按钮（仅在已有孩子信息时显示）
                                if (currentChild != null) {
                                    IconButton(
                                        onClick = { startEditing() }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "编辑",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }

                                // 新增：切换孩子下拉菜单按钮（仅在有多孩子时显示）
                                if (childrenList.size > 1) {
                                    Box {
                                        IconButton(
                                            onClick = { showChildrenDropdown = true }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.SwapHoriz,
                                                contentDescription = "切换孩子",
                                                tint = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }

                                        // 孩子下拉菜单
                                        DropdownMenu(
                                            expanded = showChildrenDropdown,
                                            onDismissRequest = { showChildrenDropdown = false }
                                        ) {
                                            childrenList.forEach { child ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = child.name + if (child.isCurrentOperation) " (当前)" else "",
                                                            fontWeight = if (child.isCurrentOperation) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    },
                                                    onClick = {
                                                        showChildrenDropdown = false
                                                        // 如果点击的不是当前孩子，则切换
                                                        if (child.id != currentChild?.id) {
                                                            coroutineScope.launch {
                                                                switchCurrentChild(child.id)
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                )
            }
        ) { paddingValues ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color(0xFFF7F7F7))
                    .padding(paddingValues)
            ) {
                if (isLoading) {
                    // 加载状态
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (errorMessage != null) {
                    // 错误状态
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                            Button(onClick = { coroutineScope.launch { loadChildInfo() } }) {
                                Text("重试")
                            }
                        }
                    }
                } else {
                    // 主要内容区域
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        // 孩子信息卡片
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .background(Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                // 姓名
                                FormItem(label = "孩子姓名", isRequired = true) {
                                    if (isEditing) {
                                        TextField(
                                            value = name,
                                            onValueChange = { name = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("请输入孩子姓名") },
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = currentChild?.name ?: "未设置",
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                }

                                // 性别
                                FormItem(label = "性别", isRequired = true) {
                                    if (isEditing) {
                                        GenderSelection(
                                            selectedGender = gender,
                                            onGenderSelected = { gender = it },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                        )
                                    } else {
                                        Text(
                                            text = currentChild?.gender ?: "未设置",
                                            modifier = Modifier.padding(vertical = 16.dp),
                                            color = if (currentChild?.gender != null) MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // 出生年月
                                FormItem(label = "出生年月") {
                                    if (isEditing) {
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            TextField(
                                                value = birthYearMonth,
                                                onValueChange = { birthYearMonth = it },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { showDatePicker = true },
                                                placeholder = { Text("如：2025-11-16") },
                                                readOnly = true,
                                                trailingIcon = { 
                                                    Icon(
                                                        imageVector = Icons.Filled.CalendarToday,
                                                        contentDescription = "选择日期",
                                                        modifier = Modifier.clickable { showDatePicker = true }
                                                    ) 
                                                },
                                                colors = TextFieldDefaults.colors(
                                                    focusedContainerColor = Color.Transparent,
                                                    unfocusedContainerColor = Color.Transparent,
                                                    focusedIndicatorColor = Color.Transparent,
                                                    unfocusedIndicatorColor = Color.Transparent
                                                )
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = currentChild?.birthYearMonth ?: "未设置",
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                }

                                // 民族
                                FormItem(label = "民族") {
                                    if (isEditing) {
                                        TextField(
                                            value = ethnicity,
                                            onValueChange = { ethnicity = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("请输入民族") },
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = currentChild?.ethnicity ?: "未设置",
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                }

                                // 就读学校/园
                                FormItem(label = "就读学校/园") {
                                    if (isEditing) {
                                        TextField(
                                            value = currentSchool,
                                            onValueChange = { currentSchool = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("请输入学校名称") },
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = currentChild?.currentSchool ?: "未设置",
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                }

                                // 户籍 - 在编辑模式下也显示
                                FormItem(label = "户籍") {
                                    if (isEditing) {
                                        TextField(
                                            value = householdRegister,
                                            onValueChange = { householdRegister = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("请输入户籍信息") },
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = currentChild?.householdRegister ?: "未设置",
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                }

                                // 家中排行 - 在编辑模式下也显示
                                FormItem(label = "家中排行") {
                                    if (isEditing) {
                                        TextField(
                                            value = birthOrder,
                                            onValueChange = { birthOrder = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("如：1") },
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = currentChild?.birthOrder ?: "未设置",
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                }

                                // 出生地 - 在编辑模式下也显示
                                FormItem(label = "出生地") {
                                    if (isEditing) {
                                        TextField(
                                            value = birthPlace,
                                            onValueChange = { birthPlace = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("请输入出生地") },
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = currentChild?.birthPlace ?: "未设置",
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                }

                                // 语言环境 - 在编辑模式下也显示
                                FormItem(label = "语言环境") {
                                    if (isEditing) {
                                        TextField(
                                            value = languageEnvironment,
                                            onValueChange = { languageEnvironment = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("请输入语言环境") },
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = currentChild?.languageEnvironment ?: "未设置",
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                }

                                // 现家庭住址 - 在编辑模式下也显示
                                FormItem(label = "现家庭住址") {
                                    if (isEditing) {
                                        TextField(
                                            value = homeAddress,
                                            onValueChange = { homeAddress = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("请输入现家庭住址") },
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = currentChild?.homeAddress ?: "未设置",
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                }

                                // 睡眠爱好 - 在编辑模式下也显示
                                FormItem(label = "睡眠爱好") {
                                    if (isEditing) {
                                        TextField(
                                            value = habits,
                                            onValueChange = { habits = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("请输入睡眠爱好") },
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = currentChild?.habits ?: "未设置",
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                }

                                // 兴趣活动 - 在编辑模式下也显示
                                FormItem(label = "兴趣活动") {
                                    if (isEditing) {
                                        TextField(
                                            value = interestActivities,
                                            onValueChange = { interestActivities = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("请输入兴趣活动") },
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = currentChild?.interestActivities ?: "未设置",
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                }

                                // 身体状态 - 使用下拉单选框
                                FormItem(label = "身体状态") {
                                    if (isEditing) {
                                        // 健康状态选项列表
                                        val healthStatusOptions = listOf(
                                            Pair("EXCELLENT", "很好"),
                                            Pair("GOOD", "良好"),
                                            Pair("AVERAGE", "普通"),
                                            Pair("POOR", "较差"),
                                            Pair("VERY_POOR", "很差")
                                        )
                                        
                                        // 当前选中的显示文本
                                        val selectedHealthStatusText = healthStatusOptions.find { it.first == healthStatus }?.second ?: ""
                                        
                                        // 下拉菜单状态
                                        var expanded by remember { mutableStateOf(false) }
                                        
                                        ExposedDropdownMenuBox(
                                            expanded = expanded,
                                            onExpandedChange = { expanded = !expanded }
                                        ) {
                                            TextField(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .menuAnchor(),
                                                readOnly = true,
                                                value = selectedHealthStatusText,
                                                onValueChange = {},
                                                placeholder = { Text("请选择身体状态") },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                                colors = TextFieldDefaults.colors(
                                                    focusedContainerColor = Color.Transparent,
                                                    unfocusedContainerColor = Color.Transparent,
                                                    focusedIndicatorColor = Color.Transparent,
                                                    unfocusedIndicatorColor = Color.Transparent
                                                )
                                            )
                                            ExposedDropdownMenu(
                                                expanded = expanded,
                                                onDismissRequest = { expanded = false }
                                            ) {
                                                healthStatusOptions.forEach { option ->
                                                    DropdownMenuItem(
                                                        text = { Text(option.second) },
                                                        onClick = {
                                                            healthStatus = option.first
                                                            expanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // 非编辑模式下显示中文文本
                                        val healthStatusMap = mapOf(
                                            "EXCELLENT" to "很好",
                                            "GOOD" to "良好",
                                            "AVERAGE" to "普通",
                                            "POOR" to "较差",
                                            "VERY_POOR" to "很差"
                                        )
                                        val displayText = currentChild?.healthStatus?.let { healthStatusMap[it] } ?: "未设置"
                                        Text(
                                            text = displayText,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                }

                                // 身体状态描述 - 在编辑模式下也显示
                                FormItem(label = "身体状态描述") {
                                    if (isEditing) {
                                        TextField(
                                            value = healthDescription,
                                            onValueChange = { healthDescription = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("请输入身体状态描述（可选）") },
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent
                                            )
                                        )
                                    } else if (!currentChild?.healthDescription.isNullOrBlank()) {
                                        Text(
                                            text = currentChild?.healthDescription ?: "",
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "未设置",
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                }

                                // 过往病史 - 使用下拉单选框，支持其他选项的自定义输入
                                FormItem(label = "过往病史") {
                                    if (isEditing) {
                                        // 病史选项列表
                                        val pastIllnessOptions = listOf(
                                            Pair("NONE", "无"),
                                            Pair("ENCEPHALITIS", "脑炎"),
                                            Pair("EPILEPSY", "癫痫"),
                                            Pair("HEART_DISEASE", "心脏病"),
                                            Pair("ASTHMA", "哮喘"),
                                            Pair("ALLERGY", "过敏"),
                                            Pair("TUBERCULOSIS", "肺结核"),
                                            Pair("OTHER", "其他")
                                        )
                                        
                                        // 当前选中的显示文本
                                        val selectedPastIllnessText = pastIllnessOptions.find { it.first == pastIllness }?.second ?: ""
                                        
                                        // 下拉菜单状态
                                        var expanded by remember { mutableStateOf(false) }
                                        
                                        // 自定义输入状态
                                        var customPastIllness by remember { mutableStateOf("") }
                                        
                                        // 检查当前是否选择了其他选项
                                        val isOtherSelected = pastIllness == "OTHER"
                                        
                                        Column {
                                            ExposedDropdownMenuBox(
                                                expanded = expanded,
                                                onExpandedChange = { expanded = !expanded }
                                            ) {
                                                TextField(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .menuAnchor(),
                                                    readOnly = true,
                                                    value = selectedPastIllnessText,
                                                    onValueChange = {},
                                                    placeholder = { Text("请选择过往病史") },
                                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                                    colors = TextFieldDefaults.colors(
                                                        focusedContainerColor = Color.Transparent,
                                                        unfocusedContainerColor = Color.Transparent,
                                                        focusedIndicatorColor = Color.Transparent,
                                                        unfocusedIndicatorColor = Color.Transparent
                                                    )
                                                )
                                                ExposedDropdownMenu(
                                                    expanded = expanded,
                                                    onDismissRequest = { expanded = false }
                                                ) {
                                                    pastIllnessOptions.forEach { option ->
                                                        DropdownMenuItem(
                                                            text = { Text(option.second) },
                                                            onClick = {
                                                                pastIllness = option.first
                                                                expanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                            
                                            // 当选择"其他"时，显示自定义输入框
                                            if (isOtherSelected) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                TextField(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    value = customPastIllness,
                                                    onValueChange = { customPastIllness = it },
                                                    placeholder = { Text("请输入具体病史") },
                                                    label = { Text("具体病史") },
                                                    colors = TextFieldDefaults.colors(
                                                        focusedContainerColor = Color.Transparent,
                                                        unfocusedContainerColor = Color.Transparent,
                                                        focusedIndicatorColor = Color.Transparent,
                                                        unfocusedIndicatorColor = Color.Transparent
                                                    )
                                                )
                                            }
                                        }
                                    } else {
                                        // 非编辑模式下显示中文文本
                                        val pastIllnessMap = mapOf(
                                            "NONE" to "无",
                                            "ENCEPHALITIS" to "脑炎",
                                            "EPILEPSY" to "癫痫",
                                            "HEART_DISEASE" to "心脏病",
                                            "ASTHMA" to "哮喘",
                                            "ALLERGY" to "过敏",
                                            "TUBERCULOSIS" to "肺结核",
                                            "OTHER" to "其他"
                                        )
                                        
                                        // 检查是否是"其他"选项
                                        val isOther = currentChild?.pastIllness == "OTHER"
                                        // 如果是"其他"选项，需要从pastIllnessDescription字段获取具体内容
                                        val displayText = if (isOther && !currentChild?.healthDescription.isNullOrEmpty()) {
                                            "其他: ${currentChild?.healthDescription}"
                                        } else {
                                            currentChild?.pastIllness?.let { pastIllnessMap[it] } ?: "未设置"
                                        }
                                        
                                        Text(
                                            text = displayText,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                }



                                // 父亲电话 - 在编辑模式下也显示
                                FormItem(label = "父亲电话") {
                                    if (isEditing) {
                                        TextField(
                                            value = fatherPhone,
                                            onValueChange = { fatherPhone = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("请输入父亲电话") },
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = currentChild?.fatherPhone ?: "未设置",
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                }

                                // 母亲电话 - 在编辑模式下也显示
                                FormItem(label = "母亲电话") {
                                    if (isEditing) {
                                        TextField(
                                            value = motherPhone,
                                            onValueChange = { motherPhone = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("请输入母亲电话") },
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = currentChild?.motherPhone ?: "未设置",
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                }

                                // 监护人电话 - 在编辑模式下也显示
                                FormItem(label = "监护人电话") {
                                    if (isEditing) {
                                        TextField(
                                            value = guardianPhone,
                                            onValueChange = { guardianPhone = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("请输入监护人电话") },
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = currentChild?.guardianPhone ?: "未设置",
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // 日期选择器对话框
            if (showDatePicker) {
                AlertDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        Button(
                            onClick = {
                                // 格式化日期为yyyy-MM-dd
                                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                birthYearMonth = dateFormat.format(java.util.Date(selectedDate))
                                showDatePicker = false
                            }
                        ) {
                            Text("确定")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showDatePicker = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("取消")
                        }
                    },
                    title = { Text("选择出生年月") },
                    text = {
                        // 使用AndroidView来包装原生CalendarView
                        AndroidView(
                            factory = {
                                val calendarView = android.widget.CalendarView(it)
                                calendarView.date = selectedDate
                                calendarView.setOnDateChangeListener(object :
                                    android.widget.CalendarView.OnDateChangeListener {
                                    override fun onSelectedDayChange(
                                        view: android.widget.CalendarView,
                                        year: Int,
                                        month: Int,
                                        dayOfMonth: Int
                                    ) {
                                        val calendar = java.util.Calendar.getInstance()
                                        calendar.set(year, month, dayOfMonth)
                                        selectedDate = calendar.timeInMillis
                                    }
                                })
                                calendarView
                            }
                        )
                    }
                )
            }
        }
    }
}