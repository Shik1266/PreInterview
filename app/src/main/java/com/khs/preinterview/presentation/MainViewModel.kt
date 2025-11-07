package com.khs.preinterview.presentation

import androidx.lifecycle.ViewModel
import androidx.work.BackoffPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.khs.preinterview.data.local.db.LocationDao
import com.khs.preinterview.data.local.model.LocationModel
import com.khs.preinterview.data.local.worker.LocationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

/**
 * UI와 데이터 계층(DAO, WorkManager) 사이의 상태와 비즈니스 로직을 처리하는 ViewModel입니다.
 * [HiltViewModel]로 Hilt 의존성 주입을 활성화합니다.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    locationDao: LocationDao,
    private val workManager: WorkManager
) : ViewModel() {

    // 데이터베이스의 모든 위치 기록을 Flow로 노출하여 UI가 실시간으로 관찰할 수 있도록 합니다.
    val locationRecords: Flow<List<LocationModel>> = locationDao.getAllRecords()

    /**
     * LocationWorker를 실행하도록 WorkManager에 요청합니다.
     * 위치 저장은 백그라운드에서 한 번만 실행되는 작업으로 설정됩니다.
     */
    fun enqueueLocationWork() {
        val locationWorkRequest: OneTimeWorkRequest =
            OneTimeWorkRequest.Builder(LocationWorker::class.java)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    10,
                    TimeUnit.SECONDS
                )
                .build()

        workManager.enqueue(locationWorkRequest)
    }
}