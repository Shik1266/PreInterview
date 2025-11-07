package com.khs.preinterview.data.local.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.khs.preinterview.data.local.db.LocationDao
import com.khs.preinterview.data.local.model.LocationModel
import kotlinx.coroutines.flow.Flow

/**
 * Room 데이터베이스의 메인 클래스.
 */
@Database(entities = [LocationModel::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
}

/**
 * Room Persistence Library의 DAO(Data Access Object) 인터페이스.
 * 데이터베이스에 접근하여 CRUD 작업을 수행하는 메서드를 정의합니다.
 */
@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(record: LocationModel)

    @Query("SELECT * FROM location ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<LocationModel>>
}