package com.example.myapplication.compose // Your package name

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.data.MileageEntry
import com.example.myapplication.ui.theme.onSurfaceDark
import com.example.myapplication.ui.theme.onSurfaceLight
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun MileageLineChartWithMP(entries: List<MileageEntry>, modifier: Modifier = Modifier) {
  if (entries.isEmpty()) {
    // You can keep your existing placeholder Box here if you want
    // Or let the caller handle the empty state
    return
  }
  val sortedEntries = remember(entries) { entries.sortedBy { it.date } }
  val firstDateTimestamp = remember(sortedEntries) { sortedEntries.firstOrNull()?.date?.time ?: 0L }

  // Convert your MileageEntry data to MPAndroidChart's Entry format
  val chartEntries =
      remember(sortedEntries, firstDateTimestamp) {
        sortedEntries.mapIndexed { _, mileageEntry ->
          // X value: time in days from the first entry (or any consistent unit)
          // MPAndroidChart works best with float values for x.
          // Using milliseconds directly can make the chart too wide or hard to manage.
          // Converting to days since the first entry can be a good approach.
          val timeDiffMillis = mileageEntry.date.time - firstDateTimestamp
          val daysSinceFirst = TimeUnit.MILLISECONDS.toDays(timeDiffMillis).toFloat()

          Entry(daysSinceFirst, mileageEntry.miles.toFloat())
        }
      }

  val onSurface = if (isSystemInDarkTheme()) onSurfaceDark.hashCode() else onSurfaceLight.hashCode()

  // Use AndroidView to embed the MPAndroidChart LineChart
  AndroidView(
      factory = { context ->
        LineChart(context).apply {
          // Basic chart setup
          description.isEnabled = false
          setDrawGridBackground(false)
          isDragEnabled = true
          setScaleEnabled(true)
          setPinchZoom(true)

          // X-Axis setup
          xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f // Show a label for each day (adjust as needed)
            setDrawGridLines(false)
            textColor = onSurface
            valueFormatter =
                object : ValueFormatter() {
                  private val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

                  override fun getFormattedValue(value: Float): String {
                    // Convert 'value' (daysSinceFirst) back to a date
                    val millis = firstDateTimestamp + TimeUnit.DAYS.toMillis(value.toLong())
                    return dateFormat.format(Date(millis))
                  }
                }
          }

          // Y-Axis (Left) setup
          axisLeft.apply {
            setDrawGridLines(false)
            textColor = onSurface
          }

          // Y-Axis (Right) setup - disable if not needed
          axisRight.isEnabled = false
          axisRight.textColor = onSurface

          // Legend setup
          legend.isEnabled = true // Or false if you don't need it
          legend.textColor = onSurface
        }
      },
      update = { lineChart ->
        // This block is called when 'entries' (and thus 'chartEntries') changes
        val lineDataSet =
            LineDataSet(chartEntries, "Mileage").apply {
              color = onSurface
              valueTextColor = onSurface
              lineWidth = 1.5f
              setDrawCircles(false)
              setCircleColor(onSurface)
              setDrawValues(false) // Don't draw mileage value on top of each point
            }

        lineChart.data = LineData(lineDataSet)

        lineChart.invalidate()
      },
      modifier = modifier)
}
