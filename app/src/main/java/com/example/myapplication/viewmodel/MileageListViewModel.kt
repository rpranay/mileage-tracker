package com.example.myapplication.viewmodel

import AppDatabase
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.MileageEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MileageListViewModel(private val context: Context) : ViewModel() {

  private val mileageEntryDao = AppDatabase.getDatabase(context).mileageEntryDao()

  /**
   * A StateFlow that holds the current list of all mileage entries from the database. It
   * automatically updates when the underlying database table changes.
   * - `viewModelScope`: The coroutine scope tied to this ViewModel's lifecycle.
   * - `SharingStarted.WhileSubscribed(5000)`: The upstream flow (database query) starts when
   *   there's at least one collector and stops 5 seconds after the last collector unsubscribes.
   *   This helps save resources.
   * - `emptyList()`: The initial value before the database query completes.
   */
  val allMileageEntries: StateFlow<List<MileageEntry>> =
      mileageEntryDao
          .getAllEntries()
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.Companion.WhileSubscribed(5000L),
              initialValue = emptyList())

  fun addMileageEntry(entry: MileageEntry) {
    viewModelScope.launch(Dispatchers.IO) { mileageEntryDao.insert(entry) }
  }

  fun deleteMileageEntry(entry: MileageEntry) {
    viewModelScope.launch(Dispatchers.IO) { mileageEntryDao.delete(entry) }
  }

  // If you need other operations (add, delete), you would add them here,
  // similar to the previous, more complete ViewModel example.
}
