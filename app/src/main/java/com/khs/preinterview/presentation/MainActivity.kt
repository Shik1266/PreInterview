package com.khs.preinterview.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.khs.preinterview.data.local.model.LocationModel
import com.khs.preinterview.R
import com.khs.preinterview.TimeUtil
import com.khs.preinterview.databinding.ActivityMainBinding
import com.khs.preinterview.databinding.CustomMarkerBinding
import com.khs.preinterview.toBitmap
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 지도 표시 및 위치 기록을 관리하는 메인 Activity입니다.
 * [AndroidEntryPoint]로 Hilt 의존성 주입을 활성화합니다.
 * [OnMapReadyCallback]을 구현하여 NaverMap 초기화 이벤트를 처리합니다.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding

    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private val markerList = mutableListOf<Marker>()        // 현재 지도에 추가된 마커 목록
    private var marketHistory = emptyList<LocationModel>()  // 지도에 이미 표시된 기록 목록
    private val currentWindow = InfoWindow()        // 현재 열려있는 정보 창

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationSource: FusedLocationSource

    val isBasic = MutableLiveData(true)     // 지도 타입(기본/위성)을 DataBinding으로 제어하기 위한 LiveData

    private val viewModel: MainViewModel by viewModels()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        private const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1001
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this

        binding.lifecycleOwner = this
        mapView = binding.mapView

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        requestLocationPermissions()
        locationSource = FusedLocationSource(
            this,
            LOCATION_PERMISSION_REQUEST_CODE
        )
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 현 위치 저장 버튼 클릭 리스너
        binding.tvSave.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                viewModel.enqueueLocationWork()
                Toast.makeText(this, "현 위치 저장 작업 요청됨.", Toast.LENGTH_SHORT).show()
            } else {
                requestLocationPermissions()
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 지도가 준비되었을 때 호출되는 콜백 (onMapReadyCallback)
     */
    override fun onMapReady(map: NaverMap) {
        naverMap = map
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Face   //NONE,NOFOLLOW,FOLLOW,FACE

        // 지도 설정
        naverMap.uiSettings.isZoomControlEnabled = true      //확대/축소 사용
        naverMap.uiSettings.isCompassEnabled = true         //나침반 사용
        naverMap.uiSettings.isScaleBarEnabled = true        //축적 바 사용
        naverMap.uiSettings.isLocationButtonEnabled = false   //xml에서 커스텀 버튼 사용

        binding.locationBtn.map = naverMap      // XML의 LocationButtonView에 NaverMap 인스턴스 연결

        naverMap.setOnMapClickListener { point, latLng ->
            currentWindow.close()
        }

        observeLocationData()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestBackgroundLocationPermission()
            }

            if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
                if (!locationSource.isActivated) {
                    naverMap.locationTrackingMode = LocationTrackingMode.None
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun checkLocationPermission() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermissions() {
        if (!checkLocationPermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            requestBackgroundLocationPermission()
        }
    }

    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    /**
     * ViewModel의 위치 기록 Flow를 관찰하여 변경 사항이 있을 때마다 지도 마커를 업데이트합니다.
     */
    private fun observeLocationData() {
        lifecycleScope.launch {
            viewModel.locationRecords.collectLatest { records ->
                updateMapMarkers(records)
            }
        }
    }


    /**
     * 데이터베이스에서 가져온 위치 기록을 기반으로 지도 마커를 추가합니다.
     */
    private fun updateMapMarkers(records: List<LocationModel>) {
        if (!::naverMap.isInitialized) return

        // 이미 지도에 표시된 기록을 제외한 새로운 기록만 필터링합니다.
        val newRecords = records.filter { it !in marketHistory }
        if (newRecords.isEmpty()) {
            return
        }

        newRecords.forEach { record ->
            val marker = Marker().apply {
                position = LatLng(record.latitude, record.longitude)
                icon = createCustomMarker(this@MainActivity, record.id.toString())
                width = 60
                height = 60
                map = naverMap

                // 마커 클릭 리스너 설정: 정보창(InfoWindow) 표시/닫기 로직
                onClickListener = Overlay.OnClickListener {
                    currentWindow.adapter =
                        object : InfoWindow.DefaultTextAdapter(this@MainActivity) {
                            override fun getText(infoWindow2: InfoWindow): CharSequence {
                                return TimeUtil.formatFromMillis(record.timestamp)
                            }
                        }
                    if (currentWindow.marker == this) {
                        currentWindow.close()
                    } else {
                        currentWindow.open(this)
                    }
                    true
                }
            }
            markerList.add(marker)
        }
        marketHistory = records
    }

    /**
     * DataBinding을 사용하여 마커에 표시될 커스텀 View를 비트맵으로 변환합니다.
     */
    private fun createCustomMarker(context: Context, text: String): OverlayImage {
        val binding = DataBindingUtil.inflate<CustomMarkerBinding>(
            LayoutInflater.from(context),
            R.layout.custom_marker,
            null,
            false
        )

        binding.markerText = text
        binding.executePendingBindings()

        val markerView = binding.root
        val bitmap = markerView.toBitmap()
        return OverlayImage.fromBitmap(bitmap)
    }

    /**
     * 지도 타입(기본/위성)을 토글하고 NaverMap에 적용합니다.
     */
    fun toggleMapType() {
        isBasic.value = !(isBasic.value ?: false)
        naverMap.mapType =
            if (isBasic.value ?: true) NaverMap.MapType.Basic else NaverMap.MapType.Satellite
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart(); binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume(); binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause(); binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop(); binding.mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy(); binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory(); binding.mapView.onLowMemory()
    }
}