package com.example.myapplication.imageParser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import com.example.myapplication.data.MileageEntry
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageParser(val imageUri: Uri, val context: Context) {
  lateinit var bitmap: Bitmap

  fun initBitmap() {
    bitmap =
        if (Build.VERSION.SDK_INT < 28) {
              MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            } else {
              val source = ImageDecoder.createSource(context.contentResolver, imageUri)
              ImageDecoder.decodeBitmap(source)
            }
            .copy(Bitmap.Config.ARGB_8888, true)
  }

  fun processImageForMileageEntry(onSuccess: (MileageEntry) -> Unit, onError: (String) -> Unit) {
    val image: InputImage
    try {
      image = InputImage.fromFilePath(context, imageUri) // Or fromBitmap(bitmap)

      val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

      recognizer
          .process(image)
          .addOnSuccessListener { visionText ->
            // Task completed successfully
            val extractedText = visionText.text
            // Now proceed to Step 4: Text Parsing
            parseMileageFromOcrText(extractedText, onSuccess, onError)
          }
          .addOnFailureListener { e ->
            // Task failed with an exception
            onError("OCR Failed: ${e.localizedMessage}")
          }
    } catch (e: Exception) {
      onError("Error preparing image for OCR: ${e.localizedMessage}")
      return
    }
  }

  private fun getPhotoDateTaken(): Date {
    try {
      context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
        val exifInterface = ExifInterface(inputStream)

        // Primary EXIF tag for original date/time
        val dateTimeOriginal = exifInterface.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
        // EXIF date format is typically "yyyy:MM:dd HH:mm:ss"
        // We need to parse this and then reformat it.
        val commonParser = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
        parseDate(dateTimeOriginal, commonParser)?.also {
          return it
        }

        // Fallback to TAG_DATETIME (last modification date/time) if original is not available
        val dateTime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME)
        parseDate(dateTime, commonParser)?.also {
          return it
        }

        // You can also check TAG_GPS_DATESTAMP if location data is important
        // and often includes a date. Format is usually "yyyy:MM:dd".
        val gpsDateStamp = exifInterface.getAttribute(ExifInterface.TAG_GPS_DATESTAMP)
        val gpsDateStampParser = SimpleDateFormat("yyyy:MM:dd", Locale.getDefault())
        parseDate(gpsDateStamp, gpsDateStampParser)?.also {
          return it
        }
      }
    } catch (e: IOException) {
      println("Error reading EXIF data: ${e.message}")
      // Handle error (e.g., file not found, not an image)
    }
    return Date() // Creates a new Date object with the current time
  }

  private fun parseDate(dateTime: String?, parser: SimpleDateFormat): Date? {
    dateTime?.also {
      try {
        val date = parser.parse(dateTime)
        return date
      } catch (e: Exception) {
        // Log error or try next tag
        println("Error parsing TAG_DATETIME_ORIGINAL: $dateTime - ${e.message}")
      }
    }
    return null
  }

  private fun parseMileageFromOcrText(
      ocrText: String,
      onSuccess: (MileageEntry) -> Unit,
      onError: (String) -> Unit
  ) {
    // 1. Look for sequences of digits (potentially with commas)
    // Regex to find numbers, possibly with commas. Adjust if miles can have decimals.
    val mileageRegex = Regex("\\d+")

    val potentialMileages = mutableListOf<Int>()

    // Iterate through lines or blocks to find numbers
    // visionText.textBlocks is often more structured
    // For simplicity here, we'll use the whole text, but iterating blocks/lines is better.
    mileageRegex.findAll(ocrText).forEach { matchResult ->
      val numberString = matchResult.value.replace(",", "") // Remove commas
      try {
        // Further validation: is it a plausible mileage number?
        // (e.g., length, not a year, not a time)
        val number = numberString.toDouble().toInt() // Convert to Int, ignore decimals for now

        // Example simple validation: mileage is usually > 3 digits and < 7-8 digits
        if (numberString.length >= 3 &&
            numberString.length <= 7 &&
            number > 100) { // Adjust range as needed
          potentialMileages.add(number)
        }
      } catch (e: NumberFormatException) {
        // Not a valid number, ignore
      }
    }

    if (potentialMileages.isNotEmpty()) {
      // How to choose the best one?
      // - Longest number?
      // - Number near keywords like "miles", "ODO"? (More advanced parsing)
      // - For now, let's assume the largest valid number is the mileage.
      val bestGuessMileage = potentialMileages.maxOrNull()
      if (bestGuessMileage != null) {
        onSuccess(MileageEntry(miles = bestGuessMileage, date = getPhotoDateTaken()))
      } else {
        onError("Could not confidently extract mileage.")
      }
    } else {
      onError("No potential mileage figures found in the image.")
    }
  }
}
