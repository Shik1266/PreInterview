package com.khs.preinterview

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.naver.maps.map.NaverMapSdk
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject

/**
 * Hilt 기반 애플리케이션 클래스입니다.
 * [HiltAndroidApp] 어노테이션을 통해 Hilt 컴포넌트 생성을 트리거합니다.
 * [Configuration.Provider]를 구현하여 Hilt가 Worker를 주입할 수 있도록 WorkManager를 초기화합니다.
 */
@HiltAndroidApp
class BaseApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NcpKeyClient(BuildConfig.NAVER_CLIENT_ID)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}