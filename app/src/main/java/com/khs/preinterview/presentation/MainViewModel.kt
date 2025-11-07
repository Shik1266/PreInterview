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

@HiltViewModel
class MainViewModel @Inject constructor(
    locationDao: LocationDao,
    private val workManager: WorkManager
) : ViewModel() {

    val locationRecords: Flow<List<LocationModel>> = locationDao.getAllRecords()

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