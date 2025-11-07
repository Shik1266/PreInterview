package com.khs.preinterview.data.local.worker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.khs.preinterview.data.local.db.LocationDao
import com.khs.preinterview.data.local.model.LocationModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * WorkManager를 사용하여 백그라운드에서 위치 정보를 가져와 데이터베이스에 저장하는 Worker입니다.
 * [HiltWorker]와 [AssistedInject]를 사용하여 LocationDao를 주입받습니다.
 */
@HiltWorker
class LocationWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val locationDao: LocationDao
) : CoroutineWorker(appContext, workerParams) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        return try {
            if (!hasLocationPermission()) {
                return Result.failure()
            }
            val location: Location = getCurrentLocationOnce()
            val record = LocationModel(
                latitude = location.latitude,
                longitude = location.longitude
            )
            locationDao.insert(record)  //데이터베이스에 삽입합니다.
            Result.success()
        } catch (e: Exception) {
            Result.retry()  // 오류 발생 시 재시도 요청
        }
    }

    /**
     * FusedLocationProviderClient를 사용하여 한 번의 정확한 위치를 비동기적으로 요청합니다.
     * suspendCancellableCoroutine을 사용하여 콜백 기반 API를 코루틴의 suspend 함수로 변환합니다.
     */
    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocationOnce() =
        suspendCancellableCoroutine { continuation ->
            // 위치 요청 설정: 10초 내에 최대 1번의 높은 정확도의 위치를 요청
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0)
                .setDurationMillis(10000)
                .setMaxUpdates(1)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation

                    if (location != null && continuation.isActive) {
                        continuation.resume(location)
                    } else if (continuation.isActive) {
                        continuation.resumeWith(kotlin.Result.failure(Exception("Location update was null.")))
                    }

                    fusedLocationClient.removeLocationUpdates(this)
                }
            }

            // 위치 업데이트 요청 시작
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            continuation.invokeOnCancellation {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }


    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            appContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            appContext, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }
}