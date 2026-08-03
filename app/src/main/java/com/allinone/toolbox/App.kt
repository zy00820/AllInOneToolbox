package com.allinone.toolbox

import android.app.Application
import com.allinone.toolbox.utils.ShizukuManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        // V1.1.1：在 Application 启动时注册 Shizuku Binder 生命周期监听
        // 让 ShizukuProvider 传来的 Binder 实时更新到本地状态缓存
        ShizukuManager.initBinderLifecycle(this)
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
