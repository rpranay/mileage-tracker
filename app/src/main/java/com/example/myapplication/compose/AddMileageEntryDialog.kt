package com.example.myapplication.compose

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.myapplication.data.MileageEntry
import java.util.Locale

@Composable
fun AddMileageEntryDialog(entry: MileageEntry, onDismiss: () -> Unit, onConfirm: () -> Unit) {
  val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text("Confirm Mileage Entry") },
      text = {
        Column {
          Text("Miles: ${entry.miles}")
          Text("Date: ${dateFormat.format(entry.date)}")
          // You could add TextFields here if you want to allow editing
        }
      },
      confirmButton = { Button(onClick = onConfirm) { Text("Add Entry") } },
      dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
