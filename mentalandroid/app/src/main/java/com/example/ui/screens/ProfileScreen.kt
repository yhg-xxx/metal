package com.example.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Task
import com.example.splash.SplashActivity
import com.example.ui.features.ChildInfoScreenActivity
import com.example.ui.features.ProfileEditActivity
import com.example.util.DatabaseHelper
import com.example.util.IpAddressManager
import com.example.util.TaskReminderService
import com.example.ui.theme.LightGrayBackground
import com.example.ui.theme.MentalTheme
import java.text.SimpleDateFormat
import java.util.*

/**
 * 个人主页屏幕组件
 * 包含用户信息、计划清单功能、退出登录等
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    var loggedInUser by remember { mutableStateOf(dbHelper.getLoggedInUser()) }
    var username by remember { mutableStateOf(loggedInUser?.username ?: "用户${(10000..99999).random()}") }

    // 任务相关状态
    var tasks by remember { mutableStateOf(listOf<Task>()) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDescription by remember { mutableStateOf("") }
    var taskPriority by remember { mutableIntStateOf(1) }

    // 加载任务数据
    LaunchedEffect(Unit) {
        tasks = dbHelper.getAllTasks()
        // 设置每日任务提醒
        TaskReminderService.setDailyTaskReminder(context)
    }

    LaunchedEffect(key1 = loggedInUser) {
        username = loggedInUser?.username ?: "用户${(10000..99999).random()}"
    }

    // 添加任务
    val addTask = {
        if (newTaskTitle.isNotBlank()) {
            val task = Task(
                title = newTaskTitle,
                description = newTaskDescription,
                priority = taskPriority
            )
            dbHelper.addTask(task)
            tasks = dbHelper.getAllTasks()
            // 重置表单
            newTaskTitle = ""
            newTaskDescription = ""
            taskPriority = 1
            showAddTaskDialog = false
        }
    }

    // 切换任务完成状态
    val toggleTaskCompletion = { taskId: Int ->
        val task = tasks.find { it.id == taskId }
        if (task != null) {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            dbHelper.updateTask(updatedTask)
            tasks = dbHelper.getAllTasks()
        }
    }

    // 删除任务
    val deleteTask = { taskId: Int ->
        dbHelper.deleteTask(taskId)
        tasks = dbHelper.getAllTasks()
    }

    // 日期格式化
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "我的",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { /* 设置按钮点击事件 */ }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "设置",
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
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = paddingValues.calculateTopPadding(),
                bottom = 80.dp
            )
        ) {
            // 用户信息卡片
            item {
                UserInfoCard(context, loggedInUser, username)
            }

            // 退出登录按钮
            if (loggedInUser != null) {
                item {
                    LogoutButton(context, dbHelper, onLogout = { loggedInUser = null })
                }
            }

            // 计划清单标题
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "计划清单",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary)
                            .clickable {
                                showAddTaskDialog = true
                            }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "添加任务",
                            tint = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // 任务列表
            if (tasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无任务，点击右上角添加",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                tasks.forEach { task ->
                    item {
                        TaskItem(
                            task = task,
                            dateFormat = dateFormat,
                            onToggleCompletion = { toggleTaskCompletion(task.id) },
                            onDelete = { deleteTask(task.id) }
                        )
                    }
                }
            }

            // 功能菜单 - 用卡片包围
            item {
                FeatureMenuCard()
            }

            // 底部间距
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // 添加任务对话框
    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("添加新任务") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("任务标题") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newTaskDescription,
                        onValueChange = { newTaskDescription = it },
                        label = { Text("任务描述（可选）") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "优先级:")
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("低", "中", "高").forEachIndexed { index, label ->
                            val priorityValue = index + 1
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                                    .background(
                                        if (taskPriority == priorityValue) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        taskPriority = priorityValue
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (taskPriority == priorityValue) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = addTask) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("取消")
                }
            },
            containerColor = LightGrayBackground
        )
    }
}

@Composable
private fun UserInfoCard(context: Context, loggedInUser: Any?, username: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onPrimary)
            .padding(16.dp)
            .clip(RoundedCornerShape(10.dp))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 用户头像 - 可点击导航到个人信息页面
            val currentUser = loggedInUser as? com.example.model.User
            val avatarUrl = currentUser?.avatarUrl
            val processedAvatarUrl = IpAddressManager.processImageUrl(avatarUrl)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        if (loggedInUser == null) {
                            // 如果未登录，跳转到登录页
                            val intent = Intent(context, LoginActivity::class.java)
                            context.startActivity(intent)
                        } else {
                            // 已登录，进入个人资料详情
                            val intent = Intent(context, ProfileEditActivity::class.java)
                            context.startActivity(intent)
                        }
                    }
            ) {
                AsyncImage(
                    model = processedAvatarUrl,
                    contentDescription = "用户头像",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // 用户信息
            Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text(
                    text = username,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                // 移除"点击登录/注册"文字
            }


        }
    }
}

@Composable
private fun LogoutButton(context: Context, dbHelper: DatabaseHelper, onLogout: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(MaterialTheme.colorScheme.onPrimary,RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp)
                .clickable {
                    // 执行退出登录操作
                    dbHelper.logout()
                    onLogout()
                    // 重新启动应用，跳转到启动页
                    val intent = Intent(context, SplashActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "退出登录",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun FeatureMenuCard() {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column {
            // 推荐学习入口
            ProfileMenuItem(
                title = "推荐学习",
                description = "查看为您定制的学习内容",
                onClick = { /* 跳转到推荐学习页面 */ }
            )

            // 分割线
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            // 评估记录入口
            ProfileMenuItem(
                title = "评估记录",
                description = "查看心理状态评估报告",
                onClick = { /* 跳转到评估记录页面 */ }
            )

            // 分割线
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            // 孩子信息管理入口
            ProfileMenuItem(
                title = "孩子信息管理",
                description = "管理孩子的基本信息和健康状况",
                onClick = {
                    // 跳转到孩子信息管理页面
                    context.startActivity(
                        Intent(context, ChildInfoScreenActivity::class.java)
                    )
                }
            )

            // 分割线
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            // 设置入口
            ProfileMenuItem(
                title = "设置",
                description = "应用配置和个性化设置",
                onClick = { /* 设置页面尚未实现 */ }
            )
        }
    }
}

/**
 * 任务项组件
 */
@Composable
private fun TaskItem(
    task: Task,
    dateFormat: SimpleDateFormat,
    onToggleCompletion: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onPrimary)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 任务完成状态按钮
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (task.isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
                .clickable(onClick = onToggleCompletion)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            if (task.isCompleted) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "已完成",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // 任务内容
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = task.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (task.isCompleted) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1
            )
            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Text(
                text = "创建时间: ${dateFormat.format(task.createTime)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 优先级标签
        Box(
            modifier = Modifier
                .padding(4.dp)
                .background(
                    when (task.priority) {
                        3 -> MaterialTheme.colorScheme.errorContainer
                        2 -> MaterialTheme.colorScheme.surfaceVariant
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
                .clip(RoundedCornerShape(4.dp))
                .padding(4.dp)
        ) {
            Text(
                text = when (task.priority) {
                    3 -> "高"
                    2 -> "中"
                    else -> "低"
                },
                fontSize = 10.sp,
                color = when (task.priority) {
                    3 -> MaterialTheme.colorScheme.onErrorContainer
                    2 -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        // 删除按钮
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "删除任务",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
    Spacer(modifier = Modifier.height(1.dp).background(MaterialTheme.colorScheme.surfaceVariant))
}

/**
 * 个人中心菜单项组件
 */
@Composable
private fun ProfileMenuItem(title: String, description: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "进入",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MentalTheme {
        ProfileScreen()
    }
}