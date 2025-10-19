package com.example.ui.features

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.model.Counselor
import com.example.model.SearchCounselorsRequest
import com.example.network.RetrofitClient
import com.example.ui.theme.MentalTheme
import com.example.util.IpAddressManager
import com.example.util.CounselorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import android.content.Intent

class CounselorSearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MentalTheme {
                CounselorSearchScreen(
                    context = this,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

/**
 * 咨询师搜索页面
 * 提供关键词搜索和多维度筛选功能
 */
@Composable
fun CounselorSearchScreen(
    context: ComponentActivity,
    onBackClick: () -> Unit
) {
    // 状态变量
    var counselors by remember { mutableStateOf<List<Counselor>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var keyword by remember { mutableStateOf("") }
    var specializationTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var therapeuticApproachTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var serviceTypeTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var genderFilter by remember { mutableStateOf("ALL") }
    
    // 筛选弹窗状态
    var showSpecializationFilter by remember { mutableStateOf(false) }
    var showApproachFilter by remember { mutableStateOf(false) }
    var showServiceTypeFilter by remember { mutableStateOf(false) }
    
    // 可用的筛选选项
    var allSpecializations by remember { mutableStateOf<List<String>>(emptyList()) }
    var allApproaches by remember { mutableStateOf<List<String>>(emptyList()) }
    val serviceTypes = listOf("文字", "语音", "视频")
    val genderOptions = listOf("全部", "男", "女", "未知")
    val genderMap = mapOf("全部" to "ALL", "男" to "MALE", "女" to "FEMALE", "未知" to "UNKNOWN")
    
    val coroutineScope = rememberCoroutineScope()
    
    // 加载筛选选项
    fun loadFilterOptions() {
        coroutineScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    allSpecializations = RetrofitClient.apiService.getAllSpecializations()
                    allApproaches = RetrofitClient.apiService.getAllTherapeuticApproaches()
                }
            } catch (e: Exception) {
                error = "获取筛选选项失败: ${e.message}"
                Timber.e(e, "Failed to fetch filter options")
            }
        }
    }
    
    // 搜索咨询师
    fun searchCounselors() {
        coroutineScope.launch {
            isLoading = true
            try {
                withContext(Dispatchers.IO) {
                    val request = SearchCounselorsRequest(
                        keyword = if (keyword.isEmpty()) null else keyword,
                        specializationTags = if (specializationTags.isEmpty()) null else specializationTags,
                        therapeuticApproachTags = if (therapeuticApproachTags.isEmpty()) null else therapeuticApproachTags,
                        serviceTypeTags = if (serviceTypeTags.isEmpty()) null else serviceTypeTags,
                        genderFilter = if (genderFilter == "ALL") null else genderFilter
                    )
                    counselors = RetrofitClient.apiService.searchCounselors(request)
                }
            } catch (e: Exception) {
                error = "搜索咨询师失败: ${e.message}"
                Timber.e(e, "Failed to search counselors")
            } finally {
                isLoading = false
            }
        }
    }
    
    // 获取筛选选项和初始咨询师列表
    LaunchedEffect(Unit) {
        loadFilterOptions()
        searchCounselors()
    }
    
    // 清除所有筛选条件
    fun clearAllFilters() {
        keyword = ""
        specializationTags = emptyList()
        therapeuticApproachTags = emptyList()
        serviceTypeTags = emptyList()
        genderFilter = "ALL"
        searchCounselors()
    }
    
    // 切换标签选择
    fun toggleTag(tag: String, tagsList: List<String>, onUpdate: (List<String>) -> Unit) {
        if (tagsList.contains(tag)) {
            onUpdate(tagsList.filter { it != tag })
        } else {
            onUpdate(tagsList + tag)
        }
    }
    
    // 渲染标签
    @Composable
    fun Tag(text: String, isSelected: Boolean, onClick: () -> Unit) {
        Box(
            modifier = Modifier
                .padding(4.dp)
                .background(
                    if (isSelected) Color(0xFF5A67D8) else Color(0xFFE2E8F0),
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clickable { onClick() }
        ) {
            Text(
                text = text,
                color = if (isSelected) Color.White else Color.Black,
                fontSize = 12.sp
            )
        }
    }
    
    // 渲染筛选弹窗
    @Composable
    fun FilterDialog(
        title: String,
        options: List<String>,
        selectedOptions: List<String>,
        onToggleOption: (String) -> Unit,
        onDismiss: () -> Unit
    ) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = Color.White
            ) {
                Column {
                    // 弹窗标题
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .border(1.dp, Color(0xFFE2E8F0)),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Button(onClick = onDismiss) {
                            Text("确定")
                        }
                    }
                    
                    // 选项列表
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        items(options) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                                    .clickable {
                                        onToggleOption(it)
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedOptions.contains(it),
                                    onCheckedChange = { isChecked ->
                                        onToggleOption(it)
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = it)
                            }
                        }
                    }
                }
            }
        }
    }
    
    // 渲染咨询师项
    @Composable
    fun CounselorItem(counselor: Counselor, context: ComponentActivity) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .clip(RoundedCornerShape(10.dp))
                .padding(12.dp)
                .clickable {
                    // 点击跳转到详情页
                    val intent = Intent(context, CounselorDetailActivity::class.java)
                    // 使用正确的咨询师ID字段
                    intent.putExtra("counselorId", counselor.counselorId)
                    context.startActivity(intent)
                },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 咨询师头像
            val imageUrl = IpAddressManager.processImageUrl(counselor.photoUrl)
            
            AsyncImage(
                model = imageUrl,
                contentDescription = "${counselor.realName}的头像",
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(10.dp)),
                placeholder = painterResource(id = R.drawable.img),
                error = painterResource(id = R.drawable.img),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            
            // 咨询师信息
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = CounselorUtils.parseSpecialization(counselor.specialization),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = CounselorUtils.getQualificationLabel(counselor),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${counselor.realName} 从业${counselor.yearsOfExperience}年 · 咨询人数${counselor.totalSessions}人",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = CounselorUtils.getServiceLabels(counselor),
                        fontSize = 12.sp,
                        color = Color(0xFF5A67D8)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "¥${counselor.consultationFee}起",
                        fontSize = 12.sp,
                        color = Color.Red
                    )
                }
            }
            
            // 私聊按钮 - 需要阻止事件冒泡
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF5A67D8))
                    .clip(RoundedCornerShape(5.dp))
                    .clickable { 
                        // 点击事件处理（空实现，仅用于阻止事件冒泡）
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "私聊",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
    
    // 主UI布局
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // 顶部搜索栏
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF5A67D8))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 返回按钮靠左
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // 标题居中
                    Text(
                        text = "咨询师搜索",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // 右侧占位元素，保持标题居中
                    Spacer(modifier = Modifier.width(80.dp))
                }

                // 搜索框部分为：
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 搜索输入框
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp) // 增加高度到54dp
                            .background(Color.White, RoundedCornerShape(24.dp)) // 圆角也相应调整
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "搜索",
                                tint = Color.Gray,
                                modifier = Modifier.size(25.dp) // 稍微增大图标
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            TextField(
                                value = keyword,
                                onValueChange = { newKeyword: String ->
                                    keyword = newKeyword
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(), // 使用fillMaxHeight填满可用高度
                                placeholder = {
                                    Text(
                                        text = "搜索咨询师姓名、擅长领域...",
                                        color = Color.Gray,
                                        fontSize = 14.sp, // 增大占位符字体
                                        maxLines = 1
                                    )
                                },
                                textStyle = TextStyle(
                                    fontSize = 16.sp, // 增大输入文字字体
                                    color = Color.Black
                                ),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    cursorColor = Color.Black
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // 搜索按钮 - 也相应增大
                    Box(
                        modifier = Modifier
                            .size(48.dp) // 增大到48dp
                            .clip(RoundedCornerShape(24.dp)) // 圆角相应调整
                            .background(Color(0xFF5A67D8))
                            .clickable { searchCounselors() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "搜索",
                            color = Color.White,
                            fontSize = 16.sp, // 增大按钮文字
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        content = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF7F7F7))
                    .padding(it),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // 筛选条件区域
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // 当前筛选条件
                        if (specializationTags.isNotEmpty() || therapeuticApproachTags.isNotEmpty() || 
                            serviceTypeTags.isNotEmpty() || genderFilter != "ALL") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "当前筛选条件", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = "清除全部",
                                    color = Color(0xFF5A67D8),
                                    fontSize = 12.sp,
                                    modifier = Modifier.clickable { clearAllFilters() }
                                )
                            }
                            
                            // 已选标签
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                                specializationTags.forEach { tag ->
                                    Tag(text = tag, isSelected = true) {
                                        toggleTag(tag, specializationTags) { specializationTags = it }
                                    }
                                }
                                therapeuticApproachTags.forEach { tag ->
                                    Tag(text = tag, isSelected = true) {
                                        toggleTag(tag, therapeuticApproachTags) { therapeuticApproachTags = it }
                                    }
                                }
                                serviceTypeTags.forEach { tag ->
                                    Tag(text = tag, isSelected = true) {
                                        toggleTag(tag, serviceTypeTags) { serviceTypeTags = it }
                                    }
                                }
                                if (genderFilter != "ALL") {
                                    val genderText = genderOptions.find { genderMap[it] == genderFilter } ?: ""
                                    Tag(text = genderText, isSelected = true) {
                                        genderFilter = "ALL"
                                        searchCounselors()
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // 筛选选项
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // 擅长领域筛选
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .background(Color.White)
                                    .clip(RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp)
                                    .clickable { showSpecializationFilter = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "擅长领域", fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // 治疗流派筛选
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .background(Color.White)
                                    .clip(RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp)
                                    .clickable { showApproachFilter = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "治疗流派", fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // 咨询方式筛选
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .background(Color.White)
                                    .clip(RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp)
                                    .clickable { showServiceTypeFilter = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "咨询方式", fontSize = 14.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 性别筛选
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(40.dp)
                                    .background(Color.White)
                                    .clip(RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "性别", fontSize = 14.sp)
                            }
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                genderOptions.forEach { option ->
                                    Tag(
                                        text = option,
                                        isSelected = genderMap[option] == genderFilter
                                    ) {
                                        genderFilter = genderMap[option] ?: "ALL"
                                        searchCounselors()
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 咨询师列表
                item {
                    if (isLoading) {
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)) {
                            Text(text = "加载中...", modifier = Modifier.align(Alignment.Center))
                        }
                    } else if (error != null) {
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)) {
                            Text(text = error!!, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                        }
                    } else if (counselors != null && counselors!!.isNotEmpty()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                              counselors!!.forEachIndexed { index, counselor ->
                                  CounselorItem(counselor = counselor, context = context)
                                  if (index < counselors!!.size - 1) {
                                      Spacer(modifier = Modifier.height(16.dp))
                                  }
                              }
                          }
                    } else {
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)) {
                            Text(text = "暂无符合条件的咨询师", modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }
            }
        }
    )
    
    // 筛选弹窗
    if (showSpecializationFilter) {
        FilterDialog(
            title = "选择擅长领域",
            options = allSpecializations,
            selectedOptions = specializationTags,
            onToggleOption = { tag ->
                toggleTag(tag, specializationTags) { specializationTags = it }
            },
            onDismiss = {
                showSpecializationFilter = false
                searchCounselors()
            }
        )
    }
    
    if (showApproachFilter) {
        FilterDialog(
            title = "选择治疗流派",
            options = allApproaches,
            selectedOptions = therapeuticApproachTags,
            onToggleOption = { tag ->
                toggleTag(tag, therapeuticApproachTags) { therapeuticApproachTags = it }
            },
            onDismiss = {
                showApproachFilter = false
                searchCounselors()
            }
        )
    }
    
    if (showServiceTypeFilter) {
        FilterDialog(
            title = "选择咨询方式",
            options = serviceTypes,
            selectedOptions = serviceTypeTags,
            onToggleOption = { tag ->
                toggleTag(tag, serviceTypeTags) { serviceTypeTags = it }
            },
            onDismiss = {
                showServiceTypeFilter = false
                searchCounselors()
            }
        )
    }
}