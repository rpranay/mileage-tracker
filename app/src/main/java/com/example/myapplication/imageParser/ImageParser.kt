package com.example.myapplication.imageParser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

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

  fun processImageForMileage(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
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

  fun parseMileageFromOcrText(
      ocrText: String,
      onSuccess: (String) -> Unit,
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
        onSuccess(bestGuessMileage.toString())
      } else {
        onError("Could not confidently extract mileage.")
      }
    } else {
      onError("No potential mileage figures found in the image.")
    }
  }
}
