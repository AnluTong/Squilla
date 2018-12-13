package me.andrew.squilla

import android.app.WallpaperManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.internal.functions.Functions
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import me.andrew.squilla.apiservice.api

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

        api.imageList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    var size = it.data?.size ?: 0
                    if (size > 0) {
                        var img = it.data!![0].url

                    }
                }, Functions.emptyConsumer())
    }
}
