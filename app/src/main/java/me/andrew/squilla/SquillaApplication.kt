package me.andrew.squilla

import android.app.Application
import me.andrew.network.ANetwork
import me.andrew.network.OkHttpClientProvider

class SquillaApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        ANetwork.setSingletonInstance(
                ANetwork.Builder()
                        .baseUrl("http://139.159.145.76")
                        .client(OkHttpClientProvider.getOkHttpClient())
                        .build()
        )
    }
}