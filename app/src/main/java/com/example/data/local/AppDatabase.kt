package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "client_settings")
data class ClientSettingEntity(
    @PrimaryKey val macAddress: String,
    val customAlias: String,
    val deviceType: String,
    val dataQuotaBytes: Long?,
    val isBlocked: Boolean = false
)

@Entity(tableName = "usage_history")
data class UsageHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String, // e.g. "2026-10-17"
    val macAddress: String,
    val bytesDownloaded: Long,
    val bytesUploaded: Long,
    val timestamp: Long
)

@Dao
interface ClientSettingDao {
    @Query("SELECT * FROM client_settings")
    fun getAllClientSettings(): Flow<List<ClientSettingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(setting: ClientSettingEntity)

    @Query("DELETE FROM client_settings WHERE macAddress = :mac")
    suspend fun delete(mac: String)
}

@Dao
interface UsageHistoryDao {
    @Query("SELECT * FROM usage_history ORDER BY timestamp DESC LIMIT 500")
    fun getRecentHistory(): Flow<List<UsageHistoryEntity>>

    @Query("SELECT SUM(bytesDownloaded) as totalDown, SUM(bytesUploaded) as totalUp FROM usage_history")
    suspend fun getTotalHistoricalUsage(): TotalUsageTuple?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: UsageHistoryEntity)

    @Query("DELETE FROM usage_history")
    suspend fun clearAll()
}

data class TotalUsageTuple(
    val totalDown: Long?,
    val totalUp: Long?
)

@Entity(tableName = "speed_test_history")
data class SpeedTestRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val downloadMbps: Double,
    val uploadMbps: Double,
    val pingMs: Double,
    val jitterMs: Double,
    val wifiSsid: String,
    val serverName: String
)

@Dao
interface SpeedTestRecordDao {
    @Query("SELECT * FROM speed_test_history ORDER BY timestamp DESC LIMIT 100")
    fun getAllHistory(): Flow<List<SpeedTestRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: SpeedTestRecordEntity): Long

    @Query("DELETE FROM speed_test_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM speed_test_history")
    suspend fun clearAll()
}

@Database(
    entities = [ClientSettingEntity::class, UsageHistoryEntity::class, SpeedTestRecordEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientSettingDao(): ClientSettingDao
    abstract fun usageHistoryDao(): UsageHistoryDao
    abstract fun speedTestRecordDao(): SpeedTestRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wifipulse_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
