package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import java.io.IOException


class ImageClassifierHelper(
    private val model: String = "cancer_classification.tflite",
    val context: Context,
    val klasifikasiListener: KlasifikasiListener?
) {
    private var imageClassifier: ImageClassifier? = null

    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        val optionsBuilder = ImageClassifier.ImageClassifierOptions.builder()
        try {
            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                model,
                optionsBuilder.build()
            )
        } catch (e: IOException) {
            klasifikasiListener?.onError(
                "Gagal dalam memuat model. Error: ${e.message}"
            )
        }
    }

    fun classifyStaticImage(pictureUri: Uri) {
        if (imageClassifier == null) setupImageClassifier()

        try {
            val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, pictureUri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                BitmapFactory.decodeStream(context.contentResolver.openInputStream(pictureUri))
            }

            val argb8888 = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val resizedBitmap = Bitmap.createScaledBitmap(argb8888, 224, 224, true)
            val tensorImage = TensorImage.fromBitmap(resizedBitmap)

            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                .add(CastOp(DataType.FLOAT32))
                .build()

            val processedImage = imageProcessor.process(tensorImage)
            val results = imageClassifier?.classify(processedImage)

            klasifikasiListener?.onResults(results)
        } catch (e: IOException) {
            klasifikasiListener?.onError(
                "Gagal membaca Gambar dari Uri : ${e.message}"
            )
        }
    }

    interface KlasifikasiListener {
        fun onError(error: String)
        fun onResults(results: List<Classifications>?)
    }
}