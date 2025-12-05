package com.example

import android.app.Application
import coil.Coil
import timber.log.Timber
import com.example.util.ImageLoaderConfig

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 初始化Timber日志库，只记录Info、Warn和Error级别的日志
        Timber.plant(object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                // 只记录Info(4)、Warn(5)和Error(6)级别的日志
                if (priority >= android.util.Log.INFO) {
                    super.log(priority, tag, message, t)
                }
            }
        })

        // 配置Coil图片加载器
        Coil.setImageLoader(ImageLoaderConfig.createImageLoader(this))
        Timber.i("Coil图片加载器配置完成")
    }
}