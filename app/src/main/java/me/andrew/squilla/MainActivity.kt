package me.andrew.squilla

import android.app.WallpaperManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { finish() }

        tvBtn.setOnClickListener({
            try {
                val wallpaperManager = WallpaperManager.getInstance(this)
                wallpaperManager.setBitmap(ivBg.cropBitmap())
            } catch (e: Exception) {
            }
        })
    }
}
