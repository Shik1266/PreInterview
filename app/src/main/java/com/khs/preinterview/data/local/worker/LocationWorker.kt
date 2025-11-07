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
            locationDao.insert(record)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocationOnce() =
        suspendCancellableCoroutine { continuation ->
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