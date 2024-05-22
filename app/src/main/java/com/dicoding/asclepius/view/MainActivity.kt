package com.dicoding.asclepius.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.yalantis.ucrop.UCrop
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.analyzeButton.setOnClickListener {
            currentImageUri?.let {
                analyzeImage(it)
            } ?: run {
                showToast(getString(R.string.empty_picture))
            }
        }
    }

    private fun startGallery() {
        launcherGallery.launch("image/*")
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) {
            showToast(getString(R.string.media_isnt_selected))
        } else {
            val outputFile = File(cacheDir, "cropped_image_${System.currentTimeMillis()}.jpg")
            val outputUri = Uri.fromFile(outputFile)

            UCrop.of(uri, outputUri)
                .withAspectRatio(1f, 1f)
                .start(this)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            if (resultUri != null) {
                currentImageUri = resultUri
                showImage()
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val error = UCrop.getError(data!!)
            showToast(error?.message ?: "Terjadi kesalahan")
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun analyzeImage(pictureUri: Uri) {
        val imageClassifierHelper = ImageClassifierHelper(
            context = this,
            klasifikasiListener = object : ImageClassifierHelper.KlasifikasiListener {
                override fun onError(error: String) {
                    showToast(error)
                }

                override fun onResults(results: List<Classifications>?) {
                    results?.let { classifications ->
                        classifications.forEach { classification ->
                            val categoriesList = classification.categories
                            val kategori = categoriesList[0].label
                            val skor = categoriesList[0].score
                            val persenanSkor = (skor * 100).toInt()
                            val strpersenanSkor = "$persenanSkor%"
                            moveToResult(currentImageUri, kategori, strpersenanSkor)
                        }
                    }
                }
            })

        imageClassifierHelper.classifyStaticImage(pictureUri)
    }

    private fun moveToResult(
        currentImageUri: Uri?,
        kategori: String,
        persenanSkor: String,
    ) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(ResultActivity.EXTRA_PICTURE_URI, currentImageUri.toString())
            putExtra(ResultActivity.EXTRA_KATEGORI, kategori)
            putExtra(ResultActivity.EXTRA_SKOR, persenanSkor)
        }
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
