package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.example.ui.navigation.BottomNavigationBar
import com.example.ui.navigation.BottomNavigationItem
import com.example.ui.features.CounselorSearchActivity
import com.example.ui.screens.LearningScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MessageScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.MentalTheme

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 设置沉浸式状态栏
        setupImmersiveStatusBar()

        // 创建导航到搜索页面的函数
        val navigateToSearch: () -> Unit = {
            startActivity(Intent(this, CounselorSearchActivity::class.java))
        }

        // 从Intent中获取是否来自广告页面的参数
        val isFromAdScreen = intent.getBooleanExtra("FROM_AD_SCREEN", false)

        setContent {
            MentalTheme {
                // 创建主屏幕布局，包含底部导航栏和内容区域
                MainScreen(navigateToSearch = navigateToSearch, initialIsFromAdScreen = isFromAdScreen)
            }
        }
    }

    private fun setupImmersiveStatusBar() {
        // 启用边缘到边缘显示
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 让布局可以全屏，延展到状态栏里
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        // 设置状态栏颜色为透明
        window.statusBarColor = android.graphics.Color.TRANSPARENT
    }
}

/**
 * 主屏幕组件
 * 包含底部导航栏和根据选中标签显示对应内容的逻辑
 */
@Composable
fun MainScreen(navigateToSearch: () -> Unit, initialIsFromAdScreen: Boolean = false) {
    // 当前选中的标签页状态
    var selectedTab by remember { mutableStateOf(BottomNavigationItem.HOME) }

    // 创建一个状态来跟踪是否已经显示过广告弹窗
    var hasShownAdPopup by remember { mutableStateOf(false) }

    // 计算是否应该显示广告弹窗
    val shouldShowAdPopup = remember(selectedTab, initialIsFromAdScreen, hasShownAdPopup) {
        // 条件：当前是首页、是从广告页跳转、且还没有显示过弹窗
        selectedTab == BottomNavigationItem.HOME && initialIsFromAdScreen && !hasShownAdPopup
    }

    // 当shouldShowAdPopup为true时，标记为已显示
    LaunchedEffect(shouldShowAdPopup) {
        if (shouldShowAdPopup) {
            hasShownAdPopup = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // 底部导航栏组件
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        // 根据选中的标签显示对应的屏幕内容
        val modifier = Modifier.padding(paddingValues)
        when (selectedTab) {
            BottomNavigationItem.HOME -> HomeScreen(
                modifier = modifier,
                onNavigateToSearch = navigateToSearch,
                isFromAdScreen = shouldShowAdPopup
            )
            BottomNavigationItem.MESSAGE -> MessageScreen(modifier)
            BottomNavigationItem.LEARNING -> LearningScreen(modifier)
            BottomNavigationItem.PROFILE -> ProfileScreen(modifier)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MentalTheme {
        MainScreen(navigateToSearch = { /* 预览中不执行实际导航 */ }, initialIsFromAdScreen = false)
    }
}