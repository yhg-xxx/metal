package com.example

import android.app.Application

import coil.Coil
import timber.log.Timber
import com.example.util.ImageLoaderConfig

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 初始化Timber日志库
        // 由于BuildConfig可能还未生成，暂时直接初始化
        Timber.plant(Timber.DebugTree())
        

        // 配置Coil图片加载器
        Coil.setImageLoader(ImageLoaderConfig.createImageLoader(this))
        Timber.d("Coil图片加载器配置完成")
    }
}