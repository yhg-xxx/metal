package com.example.util

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 图片加载工具类
 * 提供图片加载功能的测试组件
 */
object ImageLoadingUtils {
    
    /**
     * 测试图片加载功能组件
     * 展示图片加载的测试UI和状态
     */
    @Composable
    fun TestImageLoading() {
        val context = androidx.compose.ui.platform.LocalContext.current
        val imageUrl = "http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg"
        val processedImageUrl = IpAddressManager.processImageUrl(imageUrl)
        var loadResult by remember { mutableStateOf<String?>(null) }
        val coroutineScope = rememberCoroutineScope()
        
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                performImageLoadingTest(context, imageUrl, processedImageUrl) { result ->
                    loadResult = result
                }
            }) {
                Text("测试图片加载")
            }
            
            if (loadResult != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = loadResult ?: "", 
                    fontSize = 14.sp, 
                    color = if (loadResult?.contains("成功") == true) 
                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            AsyncImage(
                model = processedImageUrl,
                contentDescription = "测试图片",
                modifier = Modifier.size(200.dp),
                placeholder = painterResource(id = R.drawable.img),
                error = painterResource(id = R.drawable.img),
                contentScale = ContentScale.Crop,
                onSuccess = { 
                    loadResult = "图片加载成功!"
                    Timber.d("图片加载成功")
                },
                onError = { 
                    loadResult = "图片加载失败"
                    Timber.e("图片加载失败")
                }
            )
        }
    }
    
    /**
     * 执行图片加载测试的逻辑方法
     * @param context Android上下文
     * @param originalUrl 原始图片URL
     * @param processedUrl 处理后的图片URL
     * @param resultCallback 结果回调函数
     */
    fun performImageLoadingTest(
        context: Context,
        originalUrl: String?,
        processedUrl: String?,
        resultCallback: (String) -> Unit
    ) {
        Timber.d("开始测试图片加载: 原始URL=$originalUrl, 处理后URL=$processedUrl")
        
        // 在协程中执行测试
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.Main) {
            try {
                // 标记测试已启动
                resultCallback("测试已启动，请查看日志...")
                
                // 模拟网络请求延迟
                withContext(Dispatchers.IO) {
                    delay(500)
                }
                
            } catch (e: Exception) {
                resultCallback("图片加载异常: ${e.message ?: "未知异常"}")
                Timber.e(e, "图片加载异常")
            }
        }
    }
    
    /**
     * 处理图片URL的工具方法
     * 直接调用IpAddressManager的处理方法
     */
    fun processImageUrl(url: String?): String {
        return IpAddressManager.processImageUrl(url ?: "") ?: ""
    }
}