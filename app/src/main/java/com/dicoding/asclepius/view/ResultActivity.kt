package com.dicoding.asclepius.view

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Hasil Prediksi Kanker"

        getInfo()
    }

    private fun getInfo() {
        val pictureUri = Uri.parse(intent.getStringExtra(EXTRA_PICTURE_URI).orEmpty())
        val kategori = intent.getStringExtra(EXTRA_KATEGORI).orEmpty()
        val skor = intent.getStringExtra(EXTRA_SKOR).orEmpty()

        binding.apply {
            pictureUri?.let {
                binding.resultImage.setImageURI(it)
            }
            resultText.text = getString(R.string.result, kategori, skor)
        }
    }

    companion object {
        const val EXTRA_PICTURE_URI = "extra_picture_uri"
        const val EXTRA_KATEGORI = "kategori"
        const val EXTRA_SKOR = "skor"
    }
}