package com.khs.preinterview.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room 데이터베이스 테이블을 나타내는 데이터 클래스 (엔티티).
 * [tableName]을 "location"으로 지정하여 테이블 이름을 설정합니다.
 */
@Entity(tableName = "location")
data class LocationModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)