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

@Database(entities = [LocationModel::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
}

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(record: LocationModel)

    @Query("SELECT * FROM location ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<LocationModel>>
}