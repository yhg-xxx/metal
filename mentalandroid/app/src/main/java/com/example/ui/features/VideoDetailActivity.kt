package com.example.ui.features

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.MediaController
import android.widget.VideoView
import coil.compose.AsyncImage
import com.example.model.LearningVideo
import com.example.network.RetrofitClient
import com.example.ui.theme.MentalTheme
import com.example.util.IpAddressManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import androidx.core.net.toUri

/**
 * 视频详情页Activity
 * 合并了Activity和Compose组件，支持视频播放功能
 */
class VideoDetailActivity : AppCompatActivity() {
    // VideoView实例
    private var videoView: VideoView? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MentalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VideoDetailContent()
                }
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        // 在暂停时停止播放，但保持资源
        videoView?.pause()
    }
    
    override fun onStop() {
        super.onStop()
        // 在停止时完全释放资源
        videoView?.stopPlayback()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        videoView?.stopPlayback()
        videoView = null
    }
    
    /**
     * 视频详情页的主要内容Composable组件
     */
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun VideoDetailContent() {
        val context = LocalContext.current
        val apiService = RetrofitClient.apiService
        val coroutineScope = rememberCoroutineScope()
        
        // 从Intent获取学习包ID和标题
        val learningPackageId = intent.getLongExtra("learningPackageId", 0)
        val learningPackageTitle = intent.getStringExtra("learningPackageTitle") ?: "学习视频"
        
        var videos by remember { mutableStateOf<List<LearningVideo>?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var selectedVideo by remember { mutableStateOf<LearningVideo?>(null) }
        
        // 加载视频列表数据
        LaunchedEffect(learningPackageId) {
            if (learningPackageId > 0) {
                isLoading = true
                try {
                    val response = withContext(Dispatchers.IO) {
                        apiService.getVideosByLearningPackageId(learningPackageId)
                    }
                    
                    if (response.code == 200) {
                        videos = response.data
                        // 默认选择第一个视频
                        if (response.data != null && true && response.data.isNotEmpty()) {
                            selectedVideo = response.data[0]
                        }
                    } else {
                        errorMessage = "获取视频列表失败: ${response.msg}"
                        Timber.tag("VideoDetailScreen").e(errorMessage.toString())
                    }
                } catch (e: Exception) {
                    errorMessage = "网络请求异常: ${e.message}"
                    Timber.tag("VideoDetailScreen").e(e, "Failed to fetch videos")
                } finally {
                    isLoading = false
                }
            }
        }
        
        // 当选中视频改变时，更新播放器
        LaunchedEffect(selectedVideo) {
            if (selectedVideo != null && videoView != null) {
                val videoUrl = IpAddressManager.processVideoUrl(selectedVideo!!.videoUrl)
                if (videoUrl != null) {
                    try {
                        // 优化视频加载流程
                        videoView?.stopPlayback()
                        // 先设置URI，再开始播放
                        videoView?.setVideoURI(videoUrl.toUri())
                        // 添加延迟，确保视频完全准备好再开始播放
                        delay(500)
                        videoView?.start()
                    } catch (e: Exception) {
                        Timber.tag("VideoPlayer").e(e, "设置视频URI失败")
                    }
                }
            }
        }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = learningPackageTitle,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "返回",
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
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(
                    top = 80.dp, // 为顶部AppBar留出空间
                    bottom = 20.dp
                )
            ) {
                // 视频播放器区域（当有选中视频时显示）
                item {
                    if (selectedVideo != null) {
                        VideoPlayerContainer(video = selectedVideo!!)
                    } else if (isLoading) {
                        LoadingContainer()
                    } else if (errorMessage != null) {
                        ErrorContainer(errorMessage = errorMessage!!)
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color.Gray))
                    }
                }
                
                // 视频列表标题
                item {
                    Text(
                        text = "课程列表",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                // 视频列表
                if (videos != null && videos!!.isNotEmpty()) {
                    items(videos!!) { video ->
                        VideoItem(
                            video = video,
                            isSelected = selectedVideo?.id == video.id,
                            onSelect = {
                                selectedVideo = video
                            }
                        )
                    }
                } else if (!isLoading) {
                    item {
                        EmptyContainer()
                    }
                }
            }
        }
    }
    
    /**
     * 视频播放器容器组件
     */
    @SuppressLint("DiscouragedPrivateApi")
    @Composable
    fun VideoPlayerContainer(video: LearningVideo) {
        val context = LocalContext.current
        val isBuffering = remember { mutableStateOf(false) }
        val errorMessage = remember { mutableStateOf<String?>(null) }
        val retryCount = remember { mutableIntStateOf(0) }
        
        Column(modifier = Modifier.padding(16.dp)) {
            // 使用Android原生的VideoView
            AndroidView(
                factory = {
                    VideoView(it).apply {
                        // 设置MediaController以提供播放控制
                        val mediaController = MediaController(it)
                        setMediaController(mediaController)
                        mediaController.setAnchorView(this)
                        
                        // 优化音频和视频播放
                         requestFocus()
                        
                        // 设置缓冲监听器
                        setOnPreparedListener { mediaPlayer ->
                            // 优化媒体播放器设置
                            mediaPlayer.setVideoScalingMode(android.media.MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                            mediaPlayer.setScreenOnWhilePlaying(true)
                            mediaPlayer.isLooping = false
                            
                            isBuffering.value = false
                            errorMessage.value = null
                        }
                        
                        // 设置错误监听器
                        setOnErrorListener { mp, what, extra ->
                            Timber.tag("VideoPlayer").e("视频播放错误: what=$what, extra=$extra")
                            errorMessage.value = "视频播放出错，正在重试..."
                            isBuffering.value = true
                            
                            // 实现自动重试机制
                            if (retryCount.intValue < 3) {
                                retryCount.intValue++
                                // 延迟后重试
                                Handler(Looper.getMainLooper()).postDelayed({
                                    try {
                                        // 重置并重新加载
                                        val currentUri = videoView?.let { vv ->
                                            // 尝试通过反射获取当前URI
                                            try {
                                                val field = VideoView::class.java.getDeclaredField("mUri")
                                                field.isAccessible = true
                                                field.get(vv) as Uri?
                                            } catch (e: Exception) {
                                                null
                                            }
                                        }
                                        
                                        if (currentUri != null) {
                                            stopPlayback()
                                            setVideoURI(currentUri)
                                            start()
                                        }
                                    } catch (e: Exception) {
                                        Timber.tag("VideoPlayer").e(e, "重试失败")
                                    }
                                }, 1000)
                                true
                            } else {
                                retryCount.intValue = 0
                                isBuffering.value = false
                                errorMessage.value = "视频播放失败，请检查网络连接"
                                false
                            }
                        }
                        
                        // 设置信息监听器（用于监控缓冲状态）
                        setOnInfoListener { mp, what, extra ->
                            when (what) {
                                android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                                    isBuffering.value = true
                                    true
                                }
                                android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                                    isBuffering.value = false
                                    true
                                }
                                else -> false
                            }
                        }
                        
                        // 保存到Activity变量中以便生命周期管理
                        videoView = this
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
            )
            
            // 缓冲指示器
            if (isBuffering.value) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(
                        text = "正在缓冲...",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            // 错误提示
            errorMessage.value?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(8.dp)
                )
            }
            
            // 视频信息
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Text(
                    text = video.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatDuration(video.durationSeconds),
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = video.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
    
    /**
     * 视频列表项组件
     */
    @Composable
    fun VideoItem(video: LearningVideo, isSelected: Boolean, onSelect: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect() }
                .padding(16.dp)
                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent)
                .clip(RoundedCornerShape(8.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 视频缩略图
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(45.dp)
                    .background(Color.Gray)
                    .clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = IpAddressManager.processImageUrl(video.thumbnailUrl),
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // 视频信息
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    text = video.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = formatDuration(video.durationSeconds),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // 播放按钮
            IconButton(
                onClick = { onSelect() },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "播放",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
    
    /**
     * 加载状态容器
     */
    @Composable
    fun LoadingContainer() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
                .background(Color.White)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
    
    /**
     * 错误状态容器
     */
    @Composable
    fun ErrorContainer(errorMessage: String) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
                .background(Color.White)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = errorMessage,
                color = Color.Red,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
    
    /**
     * 空状态容器
     */
    @Composable
    fun EmptyContainer() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无视频内容",
                color = Color.Gray
            )
        }
    }
    
    /**
     * 格式化视频时长（秒 -> 分钟:秒）
     */
    @SuppressLint("DefaultLocale")
    private fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }
}