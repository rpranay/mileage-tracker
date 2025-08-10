import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.MileageEntry
import kotlinx.coroutines.flow.Flow // For reactive updates

@Dao
interface MileageEntryDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(entry: MileageEntry)

  @Delete suspend fun delete(entry: MileageEntry)

  @Query("SELECT * FROM mileage_entries ORDER BY date DESC")
  fun getAllEntries(): Flow<List<MileageEntry>> // Use Flow for automatic UI updates

  @Query("SELECT * FROM mileage_entries WHERE id = :id")
  suspend fun getEntryById(id: Int): MileageEntry?

  // You can add more specific queries if needed
  @Query("SELECT COUNT(*) FROM mileage_entries") suspend fun getEntryCount(): Int
}
