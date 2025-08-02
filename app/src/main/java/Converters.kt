import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.myapplication.MileageEntry
import java.util.Date

// You'll need a TypeConverter for the Date type
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

@Database(entities = [MileageEntry::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class) // Add this to handle Date conversion
abstract class AppDatabase : RoomDatabase() {

    abstract fun mileageEntryDao(): MileageEntryDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mileage_tracker_database"
                )
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    // Migration is not covered in this basic example.
                    .fallbackToDestructiveMigration() // Be careful with this in production
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}