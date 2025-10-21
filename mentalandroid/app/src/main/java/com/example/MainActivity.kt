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
        
        setContent {
            MentalTheme {
                // 创建主屏幕布局，包含底部导航栏和内容区域
                MainScreen(navigateToSearch = navigateToSearch)
            }
        }
    }
    private fun setupImmersiveStatusBar() {
        // 启用边缘到边缘显示
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 让布局可以全屏，延展到状态栏里
        WindowCompat.getInsetsController(window, window.decorView).let { controller ->
            // 设置状态栏图标为浅色（白色），与详情页保持一致
            controller.isAppearanceLightStatusBars = false
        }

        // 设置状态栏颜色为透明
        window.statusBarColor = android.graphics.Color.TRANSPARENT
    }
}

/**
 * 主屏幕组件
 * 包含底部导航栏和根据选中标签显示对应内容的逻辑
 */
@Composable
fun MainScreen(navigateToSearch: () -> Unit) {
    // 当前选中的标签页状态
    var selectedTab by remember { mutableStateOf(BottomNavigationItem.HOME) }
    
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
                onNavigateToSearch = navigateToSearch
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
        MainScreen(navigateToSearch = { /* 预览中不执行实际导航 */ })
    }
}