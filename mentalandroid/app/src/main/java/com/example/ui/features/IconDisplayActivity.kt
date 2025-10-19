package com.example.ui.features

import android.os.Bundle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MentalTheme

/**
 * 展示Material Icons Filled包中所有图标的Activity
 */
class IconDisplayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MentalTheme {
                IconDisplayScreen()
            }
        }
    }
}

/**
 * 图标展示屏幕组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconDisplayScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Material Icons - Filled") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF5A67D8),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        // 创建图标数据列表，只使用确定可用的图标
        val iconList = listOf(
            IconData("AccountCircle", Icons.Filled.AccountCircle),
            IconData("Add", Icons.Filled.Add),
            IconData("ArrowBack", Icons.Filled.ArrowBack),
            IconData("Check", Icons.Filled.Check),
            IconData("Delete", Icons.Filled.Delete),
            IconData("Edit", Icons.Filled.Edit),
            IconData("Email", Icons.Filled.Email),
            IconData("Home", Icons.Filled.Home),
            IconData("Menu", Icons.Filled.Menu),
            IconData("MoreVert", Icons.Filled.MoreVert),
            IconData("Search", Icons.Filled.Search),
            IconData("Settings", Icons.Filled.Settings)

        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(iconList.size) {
                val icon = iconList[it]
                IconItem(icon.name, icon)
            }
        }
    }
}

/**
 * 单个图标展示项
 */
@Composable
fun IconItem(name: String, icon: IconData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon.icon?.let { imageVector ->
            Icon(
                imageVector = imageVector,
                contentDescription = name,
                modifier = Modifier.size(40.dp),
                tint = Color(0xFF5A67D8)
            )
        }
        Text(
            text = name,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * 图标数据类，包含图标名称和图标资源
 */
class IconData(val name: String, val icon: androidx.compose.ui.graphics.vector.ImageVector?)