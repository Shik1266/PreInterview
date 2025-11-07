# Kulry Pre-interview Android Location App

이 프로젝트는 **Hilt**, **Room**, **WorkManager** 그리고 **Naver Map SDK**를 사용하여 사용자 위치 기록 및 지도 시각화를 구현한 안드로이드 애플리케이션입니다. 클린 아키텍처 원칙에 따라 모듈화하여 개발되었습니다.

## 🚀 주요 기능

1.  **위치 기록:** "현위치" 버튼 클릭 시 WorkManager를 이용해 백그라운드에서 현재 위치(위도, 경도)를 획득하고 로컬 데이터베이스에 저장합니다.
2.  **위치 시각화:** Room 데이터베이스에 저장된 모든 위치 기록을 Naver Map 위에 마커로 표시합니다. 마커의 숫자는 저장 순서를 의미하고 클릭 시, 저장된 시간을 나타냅니다.
3.  **Hilt DI:** Dagger Hilt를 사용하여 의존성 주입을 처리합니다.
4.  **지도 기능:** 지도 타입 토글(기본/위성) 및 현재 위치 추적 기능을 제공합니다.

## 🛠️ 기술 스택

* **언어:** Kotlin
* **아키텍처:** MVVM (Model-View-ViewModel), 클린 아키텍처 원칙 적용 (Layered Architecture)
* **주요 라이브러리:**
    * **DI:** Dagger Hilt
    * **Persistence:** Android Room
    * **Background Task:** Android WorkManager
    * **Location:** Google Play Services Location (FusedLocationProviderClient)
    * **Mapping:** Naver Map SDK
    * **Concurrency:** Kotlin Coroutines & Flow

## 📂 아키텍처 구조

프로젝트는 다음 세 가지 주요 계층으로 구성됩니다.
### 1. Presentation Layer (`presentation`, `BaseApplication`, 유틸리티)

사용자에게 정보를 표시하고 사용자의 입력을 처리합니다.

* `MainActivity.kt`: 지도 초기화, 권한 요청, UI 이벤트 처리를 담당하는 View입니다.
* `MainViewModel.kt`: UI 관련 상태 관리 및 **WorkManager** 작업 요청을 담당합니다.
* `BaseApplication.kt`: 앱 진입점이며, **Hilt** 및 **WorkManager** 설정을 구성합니다.


### 2. Data Layer (`data`)

앱의 데이터를 관리하고 제공합니다. 로컬 데이터베이스 접근, 위치 정보 획득 등의 구체적인 구현이 포함됩니다.

* `LocationWorker.kt`: 백그라운드에서 GPS 정보를 획득하고 데이터베이스에 저장하는 WorkManager Worker입니다.
* `AppDatabase.kt`: **Room** 데이터베이스 정의입니다.
* `LocationDao.kt`: 데이터베이스에 대한 CRUD (Create, Read) 작업을 정의하는 DAO입니다.

### 3. Data Model & DI (`model`, `di`)

데이터의 구조와 의존성 주입을 담당합니다.

* `LocationModel.kt`: **Room** 엔티티로 사용되는 위치 데이터의 구조입니다.
* `AppModule.kt`: **RoomDatabase**, **LocationDao**, **WorkManager** 인스턴스를 싱글톤으로 제공하는 **Hilt DI** 모듈입니다.
## 🔑 설정 방법

1.  **Naver Map SDK 설정:**
    * `BaseApplication.kt`에 정의된 `BuildConfig.NAVER_CLIENT_ID`에 Naver Cloud Platform의 **Client ID**를 설정해야 합니다.

2.  **권한:**
    * `AndroidManifest.xml`에 `ACCESS_FINE_LOCATION` 및 `ACCESS_COARSE_LOCATION` 권한이 필요합니다.
    * Android Q(API 29) 이상에서는 백그라운드 위치 저장을 위해 `ACCESS_BACKGROUND_LOCATION` 권한이 필요하며, 이는 런타임에 요청됩니다.

## ⚙️ 실행 방법

Android Studio에서 프로젝트를 빌드하고 실행합니다. 앱 실행 후 **위치 권한을 허용**해야 위치 저장 및 지도 기능이 정상 작동합니다.
