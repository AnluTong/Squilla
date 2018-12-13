package me.andrew.squilla.apiservice

import io.reactivex.Observable
import me.andrew.network.ANetwork
import retrofit2.http.GET

var api = ANetwork.with().createIfAbsent(SquillaAPI::class.java)

interface SquillaAPI {

    @GET("/image/list")
    fun imageList(): Observable<BaseModel<List<ImagesBean>>>
}