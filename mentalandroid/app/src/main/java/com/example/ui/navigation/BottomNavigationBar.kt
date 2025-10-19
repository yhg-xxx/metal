package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person

import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

/**
 * 底部导航栏组件
 * @param selectedTab 当前选中的标签页
 * @param onTabSelected 标签页选中时的回调函数
 */
@Composable
fun BottomNavigationBar(
    selectedTab: BottomNavigationItem,
    onTabSelected: (BottomNavigationItem) -> Unit
) {
    NavigationBar(
        modifier = Modifier,
        containerColor = Color.White,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        // 遍历所有导航项
        BottomNavigationItem.entries.forEach {
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = it.icon,
                        contentDescription = it.label,
                        tint = if (selectedTab == it) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.Gray
                        }
                    )
                },
                label = {
                    Text(
                        text = it.label,
                        color = if (selectedTab == it) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.Gray
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                selected = selectedTab == it,
                onClick = { onTabSelected(it) }
            )
        }
    }
}

/**
 * 底部导航项枚举类
 */
enum class BottomNavigationItem(
    val label: String,
    val icon: ImageVector
) {
    HOME("首页", Icons.Filled.Home),
    MESSAGE("消息", Icons.Filled.Call),
    LEARNING("学习", Icons.Filled.Book),
    PROFILE("我的", Icons.Filled.Person)
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    BottomNavigationBar(
        selectedTab = BottomNavigationItem.HOME,
        onTabSelected = {}
    )
}